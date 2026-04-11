from langchain.tools import tool
from sqlalchemy import create_engine, text
from dotenv import load_dotenv
import os

load_dotenv()

engine = create_engine(os.getenv("DB_URL"))

@tool
def query_accommodations(question: str) -> str:
    """
    Use this tool to answer questions about accommodations.
    Questions about price, type, capacity, location, availability, ratings.
    Examples: 'find villas under 200 TND', 'accommodations for 4 people', 
    'best rated accommodations', 'cheap apartments in Tunis'
    """
    try:
        # Let the LLM generate SQL based on the question
        with engine.connect() as conn:
            # Get accommodation data with reviews
            result = conn.execute(text("""
                SELECT 
                    a.id,
                    a.title,
                    a.type,
                    a.adresse,
                    a.price,
                    a.capacite,
                    a.description,
                    ROUND(AVG(r.rating), 1) as avg_rating,
                    COUNT(r.id) as review_count
                FROM accommodation a
                LEFT JOIN accommodation_review r ON r.accommodation_id = a.id
                GROUP BY a.id, a.title, a.type, a.adresse, 
                         a.price, a.capacite, a.description
                ORDER BY avg_rating DESC
            """))
            
            rows = result.fetchall()
            keys = result.keys()
            
            accommodations = []
            for row in rows:
                acc = dict(zip(keys, row))
                accommodations.append(
                    f"- {acc['title']} ({acc['type']}) | "
                    f"Location: {acc['adresse']} | "
                    f"Price: {acc['price']} TND/night | "
                    f"Capacity: {acc['capacite']} persons | "
                    f"Rating: {acc['avg_rating'] or 'No ratings'}/5 "
                    f"({acc['review_count']} reviews) | "
                    f"Description: {acc['description'][:100] if acc['description'] else 'N/A'}"
                )
            
            return f"Found {len(accommodations)} accommodations:\n" + "\n".join(accommodations)
            
    except Exception as e:
        return f"Error querying database: {str(e)}"


@tool  
def get_accommodation_reviews(accommodation_id: int) -> str:
    """
    Use this tool to get reviews for a specific accommodation by its ID.
    """
    try:
        with engine.connect() as conn:
            result = conn.execute(text("""
                SELECT 
                    r.rating,
                    r.comment,
                    r.review_date,
                    u.nom,
                    u.prenom
                FROM accommodation_review r
                LEFT JOIN users u ON u.id = r.user_id
                WHERE r.accommodation_id = :id
                ORDER BY r.review_date DESC
            """), {"id": accommodation_id})
            
            rows = result.fetchall()
            keys = result.keys()
            
            if not rows:
                return f"No reviews found for accommodation ID {accommodation_id}"
            
            reviews = []
            for row in rows:
                rev = dict(zip(keys, row))
                reviews.append(
                    f"- {rev['nom']} {rev['prenom']}: "
                    f"{'⭐' * int(rev['rating'])} ({rev['rating']}/5) "
                    f"on {rev['review_date']}: {rev['comment']}"
                )
            
            return f"Reviews for accommodation {accommodation_id}:\n" + "\n".join(reviews)
            
    except Exception as e:
        return f"Error: {str(e)}"