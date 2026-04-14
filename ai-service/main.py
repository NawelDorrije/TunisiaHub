from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from langchain_groq import ChatGroq
from langchain.agents import AgentExecutor, create_tool_calling_agent
from langchain.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain.memory import ConversationBufferWindowMemory
from langsmith import traceable
from tools.sql_tool import query_accommodations, get_accommodation_reviews
from tools.rag_tool import search_documents, ingest_documents
from dotenv import load_dotenv
from sqlalchemy import create_engine, text
import os
from collections import defaultdict
from tools.rag_tool import check_bad_words_in_db
import json

load_dotenv()
print("GROQ KEY:", os.getenv("GROQ_API_KEY"))  # ← add this


engine = create_engine(os.getenv("DB_URL"))
# LangSmith tracing
os.environ["LANGCHAIN_TRACING_V2"] = "true"
os.environ["LANGCHAIN_API_KEY"] = os.getenv("LANGSMITH_API_KEY")
os.environ["LANGCHAIN_PROJECT"] = os.getenv("LANGSMITH_PROJECT", "TunisiaHub")

app = FastAPI(title="TunisiaHub AI Service")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8089", "http://localhost:4200"],
    allow_methods=["*"],
    allow_headers=["*"]
)

llm = ChatGroq(
    model="llama-3.1-8b-instant",  # ← updated model
    api_key=os.getenv("GROQ_API_KEY"),
    temperature=0.3
)
moderation_llm = ChatGroq(
    model="llama-guard-3-8b",   # free tier, safety-focused
    api_key=os.getenv("GROQ_API_KEY"),  # same key, no second account needed
    temperature=0.0
)
# Tools
tools = [query_accommodations, get_accommodation_reviews, search_documents]

# Prompt
prompt = ChatPromptTemplate.from_messages([
    ("system", """You are TunisiaHub's helpful AI assistant specializing in 
    accommodation recommendations in Tunisia.

    TunisiaHub is a Tunisian accommodation platform. For ANY question about:
    - What TunisiaHub is, its history, founding date, employees, or mission,capital
    - Company policies, rules, or procedures
    - Tourism information, travel guides, regulations
    → ALWAYS call the search_documents tool first before answering.
    
    If search_documents returns no results, say:
    "I don't have that information in my documents. For company details, 
    please contact TunisiaHub directly."
    
    NEVER say "I don't know about TunisiaHub" without first calling search_documents.

    You have access to:
    1. A database of accommodations with prices, types, locations and reviews
    2. PDF documents about Tunisia tourism, accommodation policies, AND company information
    
    Always be helpful, friendly and respond in the same language as the user.
    When recommending accommodations, always mention price, rating and location.
    """),
    MessagesPlaceholder(variable_name="chat_history"),
    ("human", "{input}"),
    MessagesPlaceholder(variable_name="agent_scratchpad")
])

# Memory — keeps last 5 exchanges
# Replace the single memory instance with a session store

session_memories: dict[str, ConversationBufferWindowMemory] = {}

def get_memory(session_id: str) -> ConversationBufferWindowMemory:
    if session_id not in session_memories:
        session_memories[session_id] = ConversationBufferWindowMemory(
            k=5,
            memory_key="chat_history",
            return_messages=True
        )
    return session_memories[session_id]

# Agent
agent = create_tool_calling_agent(llm, tools, prompt)
class ChatRequest(BaseModel):
    message: str
    session_id: str = "default"

class ChatResponse(BaseModel):
    response: str
    session_id: str

@app.post("/chat", response_model=ChatResponse)
@traceable
async def chat(request: ChatRequest):
    try:
        memory = get_memory(request.session_id)
        agent_executor = AgentExecutor(
            agent=agent,
            tools=tools,
            memory=memory,
            verbose=True,
            max_iterations=7,           # raised from 3
            max_execution_time=30,      # add this too — seconds timeout
            early_stopping_method="generate"  # add this — LLM writes a final answer instead of just stopping cold
        )
        result = agent_executor.invoke({"input": request.message})
        return ChatResponse(
            response=result["output"],
            session_id=request.session_id
        )
    except Exception as e:
        return ChatResponse(
            response=f"Sorry, I encountered an error: {str(e)}",
            session_id=request.session_id
        )
@app.post("/ingest")
async def ingest():
    """Endpoint to re-ingest documents"""
    ingest_documents()
    return {"message": "Documents ingested successfully"}

@app.get("/health")
async def health():
    return {"status": "ok", "service": "TunisiaHub AI"}


#this is the price reccomendation 

class PriceRequest(BaseModel):
    type: str
    adresse: str
    capacite: int

class PriceResponse(BaseModel):
    suggested_min: float
    suggested_max: float
    recommended: float
    reasoning: str

@app.post("/suggest-price", response_model=PriceResponse)
async def suggest_price(request: PriceRequest):
    try:
        # Fetch similar accommodations from DB
        with engine.connect() as conn:
            result = conn.execute(text("""
                SELECT 
                    a.title,
                    a.type,
                    a.adresse,
                    a.price,
                    a.capacite,
                    ROUND(AVG(r.rating), 1) as avg_rating
                FROM accommodation a
                LEFT JOIN accommodation_review r ON r.accommodation_id = a.id
                WHERE a.type = :type
                GROUP BY a.id, a.title, a.type, a.adresse, a.price, a.capacite
                ORDER BY ABS(a.capacite - :capacite)
                LIMIT 5
            """), {"type": request.type, "capacite": request.capacite})

            rows = result.fetchall()
            keys = result.keys()
            similar = [dict(zip(keys, row)) for row in rows]

        similar_text = "\n".join([
            f"- {s['title']}: {s['price']} TND/night, "
            f"capacity {s['capacite']}, rating {s['avg_rating']}/5"
            for s in similar
        ]) if similar else "No similar accommodations found yet."

        prompt = f"""You are a pricing expert for Tunisia accommodations.

Based on these similar accommodations in our database:
{similar_text}

Suggest an optimal price for a NEW accommodation with:
- Type: {request.type}
- Location: {request.adresse}
- Capacity: {request.capacite} persons

Respond ONLY with a valid JSON object, no explanation outside JSON:
{{
    "suggested_min": <minimum price as number>,
    "suggested_max": <maximum price as number>,
    "recommended": <recommended price as number>,
    "reasoning": "<brief explanation in 1-2 sentences>"
}}"""

        response = llm.invoke(prompt)
        content = response.content.strip()

        # Clean JSON if wrapped in markdown
        if "```json" in content:
            content = content.split("```json")[1].split("```")[0].strip()
        elif "```" in content:
            content = content.split("```")[1].split("```")[0].strip()

        data = json.loads(content)

        return PriceResponse(
            suggested_min=float(data["suggested_min"]),
            suggested_max=float(data["suggested_max"]),
            recommended=float(data["recommended"]),
            reasoning=data["reasoning"]
        )

    except Exception as e:
        return PriceResponse(
            suggested_min=50.0,
            suggested_max=200.0,
            recommended=100.0,
            reasoning=f"Default suggestion (AI error: {str(e)})"
        )
    

class DescriptionRequest(BaseModel):
    title: str
    type: str
    adresse: str
    capacite: int
    price: float

class DescriptionResponse(BaseModel):
    description: str

@app.post("/generate-description", response_model=DescriptionResponse)
async def generate_description(request: DescriptionRequest):
    try:
        prompt = f"""You are a professional copywriter for a Tunisian tourism platform.

Write an attractive and professional accommodation description for:
- Name: {request.title}
- Type: {request.type}
- Location: {request.adresse}
- Capacity: {request.capacite} persons
- Price: {request.price} TND/night

Requirements:
- 3 to 4 sentences maximum
- Highlight the location, comfort and value
- Use an inviting and warm tone
- Do NOT use generic filler phrases like "nestled" or "boasting"
- Respond with ONLY the description text, nothing else

Description:"""

        response = llm.invoke(prompt)
        return DescriptionResponse(description=response.content.strip())

    except Exception as e:
        return DescriptionResponse(
            description=f"Error generating description: {str(e)}"
        )


class ModerationRequest(BaseModel):
    comment: str
    rating: int

class ModerationResponse(BaseModel):
    is_appropriate: bool
    reason: str

@app.post("/moderate-review", response_model=ModerationResponse)
async def moderate_review(request: ModerationRequest):
    try:
        # Step 1 — Check bad words vectorstore
        found_bad_words = check_bad_words_in_db(request.comment)

        if found_bad_words:
            return ModerationResponse(
                is_appropriate=False,
                reason=f"Your review contains inappropriate words."
            )

        # Step 2 — LLM double check for subtle inappropriate content
        prompt = f"""You are a content moderation expert for a Tunisian tourism platform.

Review this comment for inappropriate content:
- Rating: {request.rating}/5
- Comment: "{request.comment}"

Check for:
- Offensive or insulting language (in any language: Arabic, French, English, Tunisian dialect)
- Hate speech or discrimination
- Spam or irrelevant content
- Explicit or sexual content
- Threats or harassment

Respond ONLY with a valid JSON object:
{{
    "is_appropriate": <true or false>,
    "reason": "<if inappropriate, explain why in one sentence. If appropriate, say 'Review is appropriate'>"
}}"""

        response = moderation_llm.invoke(prompt)
        content = response.content.strip()

        if "```json" in content:
            content = content.split("```json")[1].split("```")[0].strip()
        elif "```" in content:
            content = content.split("```")[1].split("```")[0].strip()

        import json
        data = json.loads(content)

        return ModerationResponse(
            is_appropriate=bool(data["is_appropriate"]),
            reason=data["reason"]
        )

    except Exception as e:
        # If AI fails → allow review (fail open)
        return ModerationResponse(
            is_appropriate=True,
            reason="Moderation service unavailable — review allowed."
        )

class RecommendationRequest(BaseModel):
    history: str
    all_accommodations: str

class RecommendationResponse(BaseModel):
    recommended_ids: list[int]
    reasoning: str

@app.post("/recommend", response_model=RecommendationResponse)
async def recommend(request: RecommendationRequest):
    try:
        prompt = f"""You are an accommodation recommendation expert for TunisiaHub.

Based on this user's viewing history:
{request.history}

From all available accommodations:
{request.all_accommodations}

Recommend exactly 3 accommodations the user would most likely enjoy.
Consider: type preferences, price range, location patterns, capacity needs.
Do NOT recommend accommodations already in the history.

Respond ONLY with valid JSON:
{{
    "recommended_ids": [<id1>, <id2>, <id3>],
    "reasoning": "<one sentence explaining the recommendation logic>"
}}"""

        response = llm.invoke(prompt)
        content = response.content.strip()

        if "```json" in content:
            content = content.split("```json")[1].split("```")[0].strip()
        elif "```" in content:
            content = content.split("```")[1].split("```")[0].strip()

        import json
        data = json.loads(content)

        return RecommendationResponse(
            recommended_ids=data["recommended_ids"][:3],
            reasoning=data["reasoning"]
        )

    except Exception as e:
        return RecommendationResponse(
            recommended_ids=[],
            reasoning=f"Could not generate recommendations: {str(e)}"
        )