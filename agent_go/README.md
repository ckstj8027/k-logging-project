# agent_go — CNAPP Agent (Go + Echo)

기존 Java(Spring Boot) agent 의 Go 포팅 버전. 기능·JSON 계약·환경변수가 동일해
서버(msa-version)와 helm 차트를 수정 없이 그대로 사용한다.

## 구조

```
main.go                  # 조립 + graceful shutdown
internal/config/         # CNAPP_SERVER_URL, CNAPP_AGENT_API_KEY, AGENT_PORT
internal/k8s/            # in-cluster → kubeconfig 폴백
internal/model/          # ClusterSnapshot (Java 와 동일한 JSON 필드)
internal/collector/      # 초기 전체 스냅샷 + informer 11종 + sanitize + 해시 중복 필터
internal/sender/         # HTTP 전송 워커 풀 (X-API-KEY)
internal/server/         # Echo 헬스체크 (/healthz, /readyz)
```

## 동시성 모델

```
informer 고루틴들 ──▶ chan(버퍼 100) ──▶ 전송 워커 고루틴 x3
```
Java 의 BlockingQueue+전송 스레드 1개 구조를 채널+워커 풀로 대체.
SIGTERM 시 informer 중지 → 큐 드레인 → 종료 순서 보장.

## 빌드 & 배포 (docker build 가 곧 컴파일 검증)

```bash
docker build -t ckstj8027/cnapp-agent:go ./agent_go
docker push ckstj8027/cnapp-agent:go

# 테스트 배포: helm values 의 image.tag 만 go 로 바꾸거나
helm upgrade cnapp-agent ./msa-version/msa-infra/helm/cnapp-agent --set image.tag=go
# 롤백은 --set image.tag=latest (Java 버전)
```

## Java 버전과의 차이

- 이미지 ~200MB → ~20MB, 메모리 수백MB → 수십MB, 기동 51초 → 1초 미만
- 전송이 워커 3개 병렬 (Java 는 1스레드)
- graceful shutdown (종료 시 큐 잔여분 전송)
- 해시가 String.hashCode(32bit) → FNV-1a(64bit) : 내부 구현이라 호환 무관, 충돌 확률 감소
