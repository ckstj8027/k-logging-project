import os
from pydantic_settings import BaseSettings
from dotenv import load_dotenv

load_dotenv()

class Settings(BaseSettings):
    PROJECT_NAME: str = "K-Secure Insight Engine"
    
    # API Keys
    OPENAI_API_KEY: str = os.getenv("OPENAI_API_KEY")
    
    # DB Connections
    # 예: postgresql://user:password@localhost:5432/cnappdb
    POSTGRES_URL: str = os.getenv("POSTGRES_URL")
    REDIS_URL: str = os.getenv("REDIS_URL")
    
    # RAG Settings
    CHROMA_PERSIST_DIR: str = "./chroma_db"
    KNOWLEDGE_BASE_PATH: str = "app/data/knowledge/security_kb.txt"
    
    # Agent Settings
    LOG_LEVEL: str = os.getenv("LOG_LEVEL", "INFO")

settings = Settings()
