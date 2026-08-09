#!/bin/bash
# =============================================================
# CNAPP DB 완전 초기화 스크립트 (CICD 서버에서 실행)
# PV/Argo 를 건드리지 않아 auto-sync 레이스가 없다.
# 사용법: bash reset-db.sh
# =============================================================
set -e

echo "[1/4] PostgreSQL: cnappdb 드롭 & 재생성"
kubectl exec deploy/postgres-deployment -- psql -U postgres -d postgres -c "DROP DATABASE IF EXISTS cnappdb WITH (FORCE);"
kubectl exec deploy/postgres-deployment -- psql -U postgres -d postgres -c "CREATE DATABASE cnappdb;"

echo "[2/4] Redis: 캐시 전체 삭제"
kubectl exec deploy/redis-deployment -- redis-cli FLUSHALL

echo "[3/4] MSA 서비스 재시작 (빈 DB 에 테이블 재생성)"
kubectl rollout restart deployment msa-ingestion msa-analysis msa-query msa-auth

echo "[4/4] 재시작 완료 대기"
kubectl rollout status deployment msa-auth --timeout=300s
kubectl rollout status deployment msa-query --timeout=300s
kubectl rollout status deployment msa-ingestion --timeout=300s
kubectl rollout status deployment msa-analysis --timeout=300s

echo ""
echo "완료. 남은 수동 단계:"
echo "  1. 프론트에서 회원가입 → 새 API Key 복사"
echo "  2. cnapp-agent values.yaml 의 auth.apiKey 를 새 키로 교체 → 커밋/푸시 → Argo sync"
echo "  3. agent 재시작: kubectl rollout restart deployment <agent-deployment-name>"
