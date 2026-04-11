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
import os

load_dotenv()
print("GROQ KEY:", os.getenv("GROQ_API_KEY"))  # ← add this

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

# Tools
tools = [query_accommodations, get_accommodation_reviews, search_documents]

# Prompt
prompt = ChatPromptTemplate.from_messages([
    ("system", """You are TunisiaHub's helpful AI assistant specializing in 
    accommodation recommendations in Tunisia. 
    
    You have access to:
    1. A database of accommodations with prices, types, locations and reviews
    2. PDF documents about Tunisia tourism and accommodation policies
    
    Always be helpful, friendly and respond in the same language as the user.
    When recommending accommodations, always mention price, rating and location.
    If asked about specific documents or policies, use the document search tool.
    If asked about accommodations, prices, availability or reviews, use the database tool.
    """),
    MessagesPlaceholder(variable_name="chat_history"),
    ("human", "{input}"),
    MessagesPlaceholder(variable_name="agent_scratchpad")
])

# Memory — keeps last 5 exchanges
memory = ConversationBufferWindowMemory(
    k=5,
    memory_key="chat_history",
    return_messages=True
)

# Agent
agent = create_tool_calling_agent(llm, tools, prompt)
agent_executor = AgentExecutor(
    agent=agent,
    tools=tools,
    memory=memory,
    verbose=True,
    max_iterations=3
)

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
        result = agent_executor.invoke({
            "input": request.message
        })
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