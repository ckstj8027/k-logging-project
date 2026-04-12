from app.core.config import settings
from app.core.database import execute_read_query
from langchain_core.tools import tool
from langchain_chroma import Chroma
from langchain_openai import OpenAIEmbeddings

# RAG 도구 정의
@tool
def security_kb_search(query: str) -> str:
    """보안 지식베이스(RAG)에서 K8s 보안 글로벌 스탠다드 및 조치 가이드를 검색합니다."""
    embeddings = OpenAIEmbeddings()
    vector_db = Chroma(
        persist_directory=settings.CHROMA_PERSIST_DIR,
        embedding_function=embeddings
    )
    # 지식베이스 검색 (제목 위주 검색 유도 가능)
    results = vector_db.similarity_search(query, k=3)
    
    formatted_results = []
    for doc in results:
        formatted_results.append(f"---\n{doc.page_content}\n(출처: {doc.metadata.get('title')})")
    
    return "\n".join(formatted_results)

# SQL 도구 정의
@tool
def db_query_tool(sql_query: str, tenant_id: int) -> str:
    """K8s 자산 및 보안 데이터베이스에서 실시간 데이터를 조회합니다. 
    반드시 'sql_query'에 tenant_id 필터링이 포함되어야 합니다.
    """
    # 런타임 보안 검증: tenant_id 필터링 누락 확인 (대소문자 및 띄어쓰기 유연하게 대응)
    query_clean = sql_query.lower().replace(" ", "")
    expected_filter = f"tenant_id={tenant_id}"
    
    if expected_filter not in query_clean:
         return f"ERROR: Security violation. All queries must include 'tenant_id = {tenant_id}' in the WHERE clause."

    try:
        results = execute_read_query(sql_query)
        return str(results) if results else "No data found for the current tenant."
    except Exception as e:
        return f"Database Error: {str(e)}"
