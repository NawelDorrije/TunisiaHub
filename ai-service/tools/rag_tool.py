from langchain.tools import tool
from langchain_community.document_loaders import PyPDFLoader
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_chroma import Chroma
from langchain_community.embeddings import SentenceTransformerEmbeddings
import os

# Initialize embeddings — free, runs locally
embeddings = SentenceTransformerEmbeddings(model_name="all-MiniLM-L6-v2")

CHROMA_DIR = "./chroma_db"
PDF_DIR = "./documents"

def ingest_documents():
    """Ingest all PDFs from documents folder into ChromaDB"""
    all_docs = []
    
    for filename in os.listdir(PDF_DIR):
        if filename.endswith('.pdf'):
            print(f"📄 Ingesting: {filename}")
            loader = PyPDFLoader(os.path.join(PDF_DIR, filename))
            docs = loader.load()
            
            splitter = RecursiveCharacterTextSplitter(
                chunk_size=1000,
                chunk_overlap=200
            )
            splits = splitter.split_documents(docs)
            all_docs.extend(splits)
    
    if all_docs:
        vectorstore = Chroma.from_documents(
            documents=all_docs,
            embedding=embeddings,
            persist_directory=CHROMA_DIR
        )
        print(f"✅ Ingested {len(all_docs)} chunks into ChromaDB")
        return vectorstore
    return None

def get_vectorstore():
    """Load existing vectorstore or create new one"""
    if os.path.exists(CHROMA_DIR):
        return Chroma(
            persist_directory=CHROMA_DIR,
            embedding_function=embeddings
        )
    return ingest_documents()

vectorstore = get_vectorstore()

@tool
def search_documents(query: str) -> str:
    """
    Use this tool to search through uploaded PDF documents.
    Use for questions about Tunisia tourism, accommodation policies,
    travel guides, regulations, or any document-based information.
    Examples: 'what are the check-in rules', 'tourism regulations in Tunisia',
    'travel tips for Tunisia'
    """
    if not vectorstore:
        return "No documents have been ingested yet. Please upload PDF documents."
    
    try:
        docs = vectorstore.similarity_search(query, k=3)
        
        if not docs:
            return "No relevant information found in documents."
        
        results = []
        for i, doc in enumerate(docs, 1):
            results.append(
                f"[Source {i} - {doc.metadata.get('source', 'Unknown')}]:\n"
                f"{doc.page_content}"
            )
        
        return "\n\n".join(results)
        
    except Exception as e:
        return f"Error searching documents: {str(e)}"