import psycopg2
from contextlib import contextmanager
from app.core.config import settings

@contextmanager
def get_db_connection():
    """공유 PostgreSQL 데이터베이스 커넥션을 제공하는 컨텍스트 매니저"""
    conn = psycopg2.connect(settings.POSTGRES_URL)
    try:
        yield conn
    finally:
        conn.close()

def execute_read_query(sql: str):
    """Read-only 쿼리 실행 유틸리티"""
    with get_db_connection() as conn:
        with conn.cursor() as cur:
            cur.execute(sql)
            if cur.description:
                columns = [desc[0] for desc in cur.description]
                return [dict(zip(columns, row)) for row in cur.fetchall()]
            return []
