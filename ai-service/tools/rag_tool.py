from langchain.tools import tool
from langchain_community.document_loaders import PyPDFLoader
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_chroma import Chroma
from langchain_community.embeddings import SentenceTransformerEmbeddings
import os

embeddings = SentenceTransformerEmbeddings(model_name="all-MiniLM-L6-v2")

CHROMA_DIR = "./chroma_db"
BAD_WORDS_CHROMA_DIR = "./chroma_bad_words_db"
PDF_DIR = "./documents/tunisia_hub_profile.pdf"
BAD_WORDS_PDF = "./documents/tunisian_bad_words.pdf.pdf"

def ingest_documents():
    """Ingest Tunisian hub profile PDF into a separate ChromaDB"""
    if not os.path.exists(PDF_DIR):
        print("⚠️ No tunisia hub profile found .")
        return None
    print("📄 ingesting tunisia_hub_profile...")
    loader = PyPDFLoader(PDF_DIR)
    docs = loader.load()
    splitter = RecursiveCharacterTextSplitter(
        chunk_size=200,
        chunk_overlap=50
    )
    splits = splitter.split_documents(docs)
    vectorstore = Chroma.from_documents(
        documents=splits,
        embedding=embeddings,
        persist_directory=CHROMA_DIR
    )
    print(f"✅ Ingested {len(splits)} Tunisa hub profile")
    return vectorstore

def ingest_bad_words():
    """Ingest Tunisian bad words PDF into a separate ChromaDB"""
    if not os.path.exists(BAD_WORDS_PDF):
        print("⚠️ No bad words PDF found.")
        return None
    print("📄 Ingesting bad words document...")
    loader = PyPDFLoader(BAD_WORDS_PDF)
    docs = loader.load()
    splitter = RecursiveCharacterTextSplitter(
        chunk_size=200,
        chunk_overlap=50
    )
    splits = splitter.split_documents(docs)
    vectorstore = Chroma.from_documents(
        documents=splits,
        embedding=embeddings,
        persist_directory=BAD_WORDS_CHROMA_DIR
    )
    print(f"✅ Ingested {len(splits)} bad word chunks")
    return vectorstore

def get_vectorstore():
    if os.path.exists(CHROMA_DIR):
        return Chroma(
            persist_directory=CHROMA_DIR,
            embedding_function=embeddings
        )
    return ingest_documents()

def get_bad_words_vectorstore():
    if os.path.exists(BAD_WORDS_CHROMA_DIR):
        return Chroma(
            persist_directory=BAD_WORDS_CHROMA_DIR,
            embedding_function=embeddings
        )
    return ingest_bad_words()

vectorstore = get_vectorstore()
bad_words_vectorstore = get_bad_words_vectorstore()

@tool
def search_documents(query: str) -> str:
    """
    Use this tool to search through uploaded PDF documents.
    Use for questions about Tunisia tourism, accommodation policies,
    travel guides, regulations, or any document-based information.
    """
    if not vectorstore:
        return "No documents have been ingested yet."
    try:
        docs = vectorstore.similarity_search(query, k=3)
        if not docs:
            return "No relevant information found in documents."
        results = []
        for i, doc in enumerate(docs, 1):
            results.append(
                f"[Source {i}]:\n{doc.page_content}"
            )
        return "\n\n".join(results)
    except Exception as e:
        return f"Error searching documents: {str(e)}"


def check_bad_words_in_db(text: str) -> list[str]:
    """Search bad words vectorstore for similar content"""
    if not bad_words_vectorstore:
        return []
    try:
        docs = bad_words_vectorstore.similarity_search(text, k=3)
        found = []
        for doc in docs:
            words = doc.page_content.strip().split()
            for word in words:
                word_clean = word.lower().strip('.,!?')
                if word_clean and word_clean in text.lower():
                    found.append(word_clean)
        return list(set(found))
    except:
        return []