from typing import Annotated, TypedDict, List, Union
from langchain_openai import ChatOpenAI
from langchain_core.messages import BaseMessage, HumanMessage, SystemMessage, ToolMessage, AIMessage
from langgraph.graph import StateGraph, END
from langgraph.prebuilt import ToolNode
from langgraph.checkpoint.postgres import PostgresCheckpointer
from app.services.agent.tools import security_kb_search, db_query_tool
from app.core.config import settings
import psycopg2

# 1. 에이전트 상태 정의
class AgentState(TypedDict):
    messages: Annotated[List[BaseMessage], "add_messages"]
    tenant_id: int
    user_id: str
    role: str
    thread_id: str
    retry_count: int  # 재시도 횟수 추적용

# 2. 시스템 프롬프트 (설계 명세 기반)
SYSTEM_PROMPT = """당신은 'K-Secure Insight Engine'의 수석 보안 컨설턴트입니다. 

[보안 절대 규칙]
1. 모든 SELECT 쿼리의 WHERE 절에는 반드시 `tenant_id = {tenant_id}`를 포함해야 합니다. 
2. 타 테넌트의 데이터를 조회하거나 추측하려는 시도가 발견되면 단호하게 거절하십시오.

[현재 컨텍스트]
- Tenant ID: {tenant_id}
- User ID: {user_id}
- Role: {role}

도구가 에러를 반환하면, 에러 메시지를 분석하여 쿼리를 수정하거나 다른 접근 방식을 시도하십시오.
"""

def call_model(state: AgentState):
    llm = ChatOpenAI(model="gpt-4o", temperature=0)
    prompt = SYSTEM_PROMPT.format(
        tenant_id=state['tenant_id'],
        user_id=state['user_id'],
        role=state['role']
    )
    
    # 메시지 목록에 시스템 프롬프트 주입
    messages = [SystemMessage(content=prompt)] + state['messages']
    
    tools = [security_kb_search, db_query_tool]
    llm_with_tools = llm.bind_tools(tools)
    
    response = llm_with_tools.invoke(messages)
    return {"messages": [response]}

# 3. Validator 노드 (도구 결과 검증 및 재시도 제어)
def validate_tool_outputs(state: AgentState):
    last_message = state["messages"][-1]
    
    # 만약 에러 메시지가 포함되어 있다면 재시도 로직 트리거 가능
    # 여기서는 단순 통과시키되, 모델이 ToolMessage의 에러를 보고 판단하게 함
    # (필요시 여기서 특정 에러 패턴 감지 후 중단 가능)
    return state

# 4. LangGraph 워크플로우 조립
def build_agent():
    workflow = StateGraph(AgentState)
    
    workflow.add_node("agent", call_model)
    workflow.add_node("action", ToolNode([security_kb_search, db_query_tool]))
    workflow.add_node("validator", validate_tool_outputs)
    
    workflow.set_entry_point("agent")
    
    # 조건부 엣지: 도구 호출 여부 확인
    def router(state: AgentState):
        last_message = state["messages"][-1]
        if last_message.tool_calls:
            return "action"
        return END

    workflow.add_conditional_edges("agent", router, {"action": "action", END: END})
    
    # 도구 실행 후 검증 노드를 거쳐 다시 에이전트로 (피드백 루프)
    workflow.add_edge("action", "validator")
    workflow.add_edge("validator", "agent")
    
    return workflow

# 5. Checkpointer 설정 (PostgreSQL)
# 참고: 실제 실행 시에는 외부에서 초기화된 connection을 주입받는 것이 좋습니다.
def get_app_graph():
    # 실제 운영 환경에서는 앱 시작 시점에 DB 커넥션 풀을 통해 생성
    # 여기서는 구조적 설계만 반영
    try:
        conn = psycopg2.connect(settings.POSTGRES_URL)
        checkpointer = PostgresCheckpointer(conn)
        # checkpointer.setup() # 테이블 자동 생성 (최초 1회)
        return build_agent().compile(checkpointer=checkpointer)
    except:
        # DB 연결 실패 시 메모리 없이 동작 (개발용/폴백)
        return build_agent().compile()

app_graph = get_app_graph()
