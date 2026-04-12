from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional
from app.services.agent.workflow import app_graph
from langchain_core.messages import HumanMessage

app = FastAPI(title="K-Secure Insight Engine API")

# 요청 스키마 정의
class QueryRequest(BaseModel):
    user_id: str
    tenant_id: int
    role: str
    thread_id: str
    query: str

class QueryResponse(BaseModel):
    answer: str
    thread_id: str

@app.post("/api/v1/chat", response_model=QueryResponse)
async def chat_with_agent(request: QueryRequest):
    """
    K-Secure Insight Engine 에이전트와 대화합니다.
    사용자의 테넌트 권한과 컨텍스트를 유지하며 분석 결과를 제공합니다.
    """
    try:
        # 초기 상태 구성
        initial_state = {
            "messages": [HumanMessage(content=request.query)],
            "tenant_id": request.tenant_id,
            "user_id": request.user_id,
            "role": request.role,
            "thread_id": request.thread_id
        }
        
        # LangGraph 에이전트 실행 (설정 주입)
        config = {"configurable": {"thread_id": request.thread_id}}
        
        # 에이전트 호출 및 결과 대기 (동기/비동기 처리 가능)
        result = app_graph.invoke(initial_state, config=config)
        
        # 마지막 메시지(에이전트의 답변) 추출
        final_answer = result["messages"][-1].content
        
        return QueryResponse(
            answer=final_answer,
            thread_id=request.thread_id
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
async def health_check():
    return {"status": "healthy", "engine": "K-Secure Insight Engine"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
