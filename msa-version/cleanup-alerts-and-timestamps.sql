-- =============================================================
-- 기존 데이터 정리 스크립트 (1회 실행)
-- 실행 방법: postgres 파드에서
--   kubectl exec -it deploy/postgres-deployment -- psql -U postgres -d cnappdb -f - < cleanup-alerts-and-timestamps.sql
-- 또는 psql 접속 후 내용 붙여넣기
-- =============================================================

-- 1. 중복 alert 정리
--    같은 (tenant, resource_type, resource_name, message, status) 조합 중
--    가장 오래된 1건만 남기고 삭제한다.
DELETE FROM alerts a
USING alerts b
WHERE a.id > b.id
  AND a.tenant_id      = b.tenant_id
  AND a.resource_type  = b.resource_type
  AND a.resource_name  = b.resource_name
  AND a.message        = b.message
  AND a.status         = b.status;

-- 2. 자산 테이블 타임스탬프 백필
--    created_at 이 비어있는 기존 행은 updated_at(과거 @UpdateTimestamp) 값으로 채우고,
--    last_seen_at 이 비어있으면 updated_at 또는 created_at 으로 채운다.
UPDATE pod_profiles        SET created_at  = COALESCE(created_at, updated_at, NOW()) WHERE created_at  IS NULL;
UPDATE node_profiles       SET created_at  = COALESCE(created_at, updated_at, NOW()) WHERE created_at  IS NULL;
UPDATE service_profiles    SET created_at  = COALESCE(created_at, updated_at, NOW()) WHERE created_at  IS NULL;
UPDATE deployment_profiles SET created_at  = COALESCE(created_at, updated_at, NOW()) WHERE created_at  IS NULL;
UPDATE namespace_profiles  SET created_at  = COALESCE(created_at, updated_at, NOW()) WHERE created_at  IS NULL;
UPDATE event_profiles      SET created_at  = COALESCE(created_at, NOW())             WHERE created_at  IS NULL;

UPDATE pod_profiles        SET last_seen_at = COALESCE(last_seen_at, updated_at, created_at) WHERE last_seen_at IS NULL;
UPDATE node_profiles       SET last_seen_at = COALESCE(last_seen_at, updated_at, created_at) WHERE last_seen_at IS NULL;
UPDATE service_profiles    SET last_seen_at = COALESCE(last_seen_at, updated_at, created_at) WHERE last_seen_at IS NULL;
UPDATE deployment_profiles SET last_seen_at = COALESCE(last_seen_at, updated_at, created_at) WHERE last_seen_at IS NULL;
UPDATE namespace_profiles  SET last_seen_at = COALESCE(last_seen_at, updated_at, created_at) WHERE last_seen_at IS NULL;
UPDATE event_profiles      SET last_seen_at = COALESCE(last_seen_at, created_at)             WHERE last_seen_at IS NULL;

-- 3. (선택) 더 이상 사용하지 않는 updated_at 컬럼 제거
--    코드에서 @UpdateTimestamp updatedAt 을 제거했으므로 이 컬럼은 고아가 된다.
--    남겨둬도 무해하니, 확실할 때만 주석을 풀고 실행할 것.
-- ALTER TABLE pod_profiles        DROP COLUMN IF EXISTS updated_at;
-- ALTER TABLE node_profiles       DROP COLUMN IF EXISTS updated_at;
-- ALTER TABLE service_profiles    DROP COLUMN IF EXISTS updated_at;
-- ALTER TABLE deployment_profiles DROP COLUMN IF EXISTS updated_at;
-- ALTER TABLE namespace_profiles  DROP COLUMN IF EXISTS updated_at;
