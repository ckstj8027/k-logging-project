import os
import re
from langchain_community.document_loaders import TextLoader
from langchain_text_splitters import CharacterTextSplitter
from langchain_openai import OpenAIEmbeddings
from langchain_chroma import Chroma
from langchain_core.documents import Document
from dotenv import load_dotenv

from app.core.config import settings

load_dotenv()

class SecurityKnowledgeIndexer:
    def __init__(self, file_path: str = settings.KNOWLEDGE_BASE_PATH, persist_directory: str = settings.CHROMA_PERSIST_DIR):
        self.file_path = file_path
        self.persist_directory = persist_directory
        self.embeddings = OpenAIEmbeddings()

    def run(self):
        print(f"Loading knowledge base from {self.file_path}...")
        
        # 1. 단일 텍스트 파일 로드
        with open(self.file_path, "r", encoding="utf-8") as f:
            text = f.read()

        # 2. 논리적 구분자(---)를 사용하여 청킹 (CharacterTextSplitter 활용)
        # 설계 명세: 논리적 블록 단위로 쪼개되, 예외 상황 방지를 위해 chunk_size 설정
        splitter = CharacterTextSplitter(
            separator="---",
            chunk_size=1000, 
            chunk_overlap=50,
            is_separator_regex=False
        )
        
        chunks = splitter.split_text(text)
        print(f"Total logical chunks created: {len(chunks)}")

        documents = []
        for chunk in chunks:
            if not chunk.strip():
                continue
            
            # 3. 메타데이터 추출 (제목 패턴: [제목]: 내용)
            title_match = re.search(r"\[제목\]:\s*(.*)", chunk)
            title = title_match.group(1).strip() if title_match else "Unknown Title"
            
            # 위험도 추출
            severity_match = re.search(r"\[위험도\]:\s*(.*)", chunk)
            severity = severity_match.group(1).strip() if severity_match else "INFO"

            # Document 객체 생성 및 메타데이터 주입
            doc = Document(
                page_content=chunk.strip(),
                metadata={
                    "title": title,
                    "severity": severity,
                    "source": os.path.basename(self.file_path)
                }
            )
            documents.append(doc)

        # 4. ChromaDB 저장
        print(f"Indexing {len(documents)} documents to ChromaDB...")
        vector_db = Chroma.from_documents(
            documents=documents,
            embedding=self.embeddings,
            persist_directory=self.persist_directory
        )
        print("Indexing completed successfully.")
        return vector_db

if __name__ == "__main__":
    # 실행 경로 예시
    kb_path = "D:/AI-server/app/data/knowledge/security_kb.txt"
    indexer = SecurityKnowledgeIndexer(kb_path)
    # indexer.run() # 실제 실행 시 OpenAI API 키 필요
