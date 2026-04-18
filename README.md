대규모 K8s 자산 수집 및 실시간 보안 관리 플랫폼
------------------------------------------------------------------------------------------------------------------------

프로젝트 개요
K-Sentry는 쿠버네티스 클러스터 내의 자산(Pod, Service 등)을 에이전트 기반으로 실시간 수집하고 
보안 취약점(Privileged 권한 탈취, 블랙리스트 포트 노출 등)을 진단하는 SaaS형 클라우드 보안 플랫폼입니다.



#
회원가입 후 발급 받은 api key로 본인의 쿠버환경에 에이전트를 배포 

![1](https://github.com/user-attachments/assets/86ca6e6a-68e7-45c6-81e1-5949f9884510)


#
에이전트가 리소스들을 서버에 보내고 서버에서 처리하는 모습 

![2](https://github.com/user-attachments/assets/f4835273-6f87-4173-a3d1-0a7522590991)



#
쿠버환경에 root user 및 Privilege Mode로 실행되는 pod 를 실행한 이후 ->정책에 의해 탐지되는 모습 

![3](https://github.com/user-attachments/assets/0f0a2037-d2ad-459d-9ab0-602fd4de3645)



#
대시보드에서 replica 최대 제한 수 10->2로 정책 변경

![55](https://github.com/user-attachments/assets/f043a163-32eb-41ed-84a4-a0d29c730cf3)

#
쿠버에서는 deployment 의 replica 수를 1 -> 3 으로 늘린다 -> 정책에 의해 탐지되는 모습 

![6](https://github.com/user-attachments/assets/4506d0b3-b7e7-41cf-be2e-e28d075bed11)


#




System Architecture
---
에이전트와 서버 간의 데이터 흐름 및 동기화 아키텍처입니다.


<img width="1305" height="715" alt="image" src="https://github.com/user-attachments/assets/79e7506a-ae24-482c-9354-ccb118efb91c" />


시스템은 크게 에이전트(Agent)와 관리 서버(Server)로 나뉘며, 메시지 큐(RabbitMQ)와 관계형 데이터베이스(PostgreSQL)를 중심으로 동작합니다.

AI서버는 LangGraph 기반 워크플로우 내에서 메인 LLM이 실시간 DB 조회와 RAG 를 도구(Tools)로서 사용해 위협 분석 및 해결책을 제시합니다.

* Agent (Client Side): K8s 클러스터 내부 리소스를 수집하여 서버로 전송합니다.
* Backend Server: 수집된 데이터를 분석하고, 정책에 따라 보안 위협을 탐지하며 알람을 생성합니다.
* AI Server: 수집된 실시간 자산 데이터와 보안 지식베이스(RAG)를 융합 분석하여, 위협 분석 및 해결책 제시.


#

---
# Agent

---

## agent의 API 서버 접근 원리

Pod가 생성될 때 Kubernetes는 컨테이너가 API 서버와 통신할 수 있도록

필요한 정보를 자동으로 주입합니다.

이건 크게 두 가지로 나뉩니다:

---

### API 서버 위치 정보

Kubelet은 다음을 기준으로 환경변수를 만듭니다:

- 현재 네임스페이스
- `default` 네임스페이스의 Service

여기서 기본적으로 존재하는 Service:

- `kubernetes.default.svc.cluster.local`

이 Service를 기반으로 아래 규칙으로 변환됩니다:

| 규칙 | 결과 |
| --- | --- |
| 서비스 이름 | kubernetes |
| 대문자 변환 | KUBERNETES |
| 접미사 추가 | `_SERVICE_HOST`, `_SERVICE_PORT` |

👉 최종 환경변수:

```
KUBERNETES_SERVICE_HOST=10.96.0.1
KUBERNETES_SERVICE_PORT=443
```

즉, **"어디로 요청할지" 알려주는 정보**

---

## 인증 정보: ServiceAccount 기반 (토큰 + CA)

Pod에 ServiceAccount가 지정되면,

Kubernetes의 Admission Controller가 Pod 정의를 자동으로 수정합니다.

### 자동으로 추가되는 것

### 1) 토큰 (인증) → "누가 요청하는가"

```
/var/run/secrets/kubernetes.io/serviceaccount/token
```

### 2) CA 인증서 (TLS 검증) → "서버가 진짜인지 검증"

```
/var/run/secrets/kubernetes.io/serviceaccount/ca.crt
```


즉 volumes , volumes mount 를 통해 실제로 컨테이너에 인증서 및 토큰 파일을 마운트합니다.

---

## 최종적으로

 API 서버의 위치 + 인증 정보가 Pod 생성 시 자동으로 주입

### 서버 정보 (환경변수)

```
KUBERNETES_SERVICE_HOST
KUBERNETES_SERVICE_PORT
```

### 인증 정보 (파일)

```
/var/run/secrets/kubernetes.io/serviceaccount/token
/var/run/secrets/kubernetes.io/serviceaccount/ca.crt
```

---


## 실제 통신 과정

컨테이너 내부에서 Java/Spring 애플리케이션이 시작되면, `io.kubernetes:client-java` 라이브러리는 다음과 같이 동작합니다.

1. 환경변수 스캔: 주입된 `HOST`와 `PORT`를 읽어 접속 URL을 생성합니다.
2. 기본 경로 탐색: 라이브러리에 하드코딩된 SA 마운트 경로(`/var/run/secrets/...`)를 뒤져 `token`과 `ca.crt`를 메모리에 올립니다.
3. API 구성 완료: 위 정보들을 조합하여 HTTPS을 형성하고 API 서버와 통신을 시작합니다.

4 TLS 헨드쉐이크 

- 이제 에이전트가 `https://10.96.0.1` (API 서버)에 접속할 때를 봅시다.

- 에이전트의 준비물: 에이전트는 전 세계 공통의 CA 리스트를 쓰지 않습니다. 대신, Kubelet이 Pod 안에 꽂아준 `/var/run/secrets/.../ca.crt`를 꺼내 듭니다. 이게 이 클러스터만의 전용 Root CA입니다.

- 서버의 증명: K8s API 서버는 에이전트에게 자신의 인증서(Cluster CA가 보증하는 API 서버의 공개키)을 보여줍니다.
- 검증: 에이전트는 마운트된 `ca.crt` 도장과 서버가 보여준 신분증 대조 이후 통신을 시작합니다.
  
5 실제 데이터 교환 

검증된 TLS 터널이 뚫린 후, 에이전트가 `리소스`를 가져오는 과정은 다음과 같은 흐름으로 진행됩니다.

1. Bearer 토큰 부착: 에이전트(Java)는 마운트된 `token` 파일의 문자열을 읽어 HTTP 헤더에 담습니다.
    - `Authorization: Bearer <JWT_TOKEN_CONTENT>`
2. API 호출 전송: TLS로 암호화된 통로를 통해 API 서버에 요청을 던집니다.
    - 예: `GET /api/v1/pods` (Pod 리스트 조회)
3. API 서버의 최종 승인: API 서버는 토큰을 보고 신원을 확인한 후, **RBAC(RoleBinding)** 설정을 확인합니다.
    - "이 토큰의 주인(`k8s-agent-sa`)이 전체 네임스페이스의 Pod를 볼 권한이 있나?"
4. 데이터 응답: 권한이 확인되면 API 서버는 JSON 데이터를 응답하고, 에이전트의 `io.kubernetes:client-java`는 이를 자바 객체(`V1PodList` 등)로 변환합니다.

6 서버로 전송 
   1. K8s API 호출 → 받은 데이터 자바 객체(`V1PodList` 등)를 Java DTO로 변환.
   2. 변환된 Java DTO들을 SnapshotBlockingQueue에 넣음.
   3. 큐에서 Java 객체를 꺼내어 JSON 문자열로 직렬화.
   4. HTTP 헤더(X-API-KEY)와 함께 서버로 전송. 




---
Server
---

erd 구조 
---

<img width="2754" height="2988" alt="image" src="https://github.com/user-attachments/assets/28b89252-e5e0-4e98-96d3-f569ab986a08" />

멀티 테넌시 구조: 모든 핵심 테이블(pod_profiles, alerts, policies, users 등)이 tenants 테이블의 id를 외래 키(tenant_id)로 가지고 있어, 고객사별 데이터를 격리하여 SaaS형 구조 설계

K8s 프로필 상세: pod_profiles 테이블의 privileged, run_as_root, allow_privilege_escalation 등 보안 스캔에 필요한 상세 필드들이 엔티티 클래스에 정확히 선언

서비스 및 포트 분리: service_profiles와 service_ports가 1:N 관계로 정규화되어 있는 구조 역시 ServiceProfile 엔티티 내의 @OneToMany 관계

---

   주요 구성 요소별 기능
---

  Controller - 인터페이스 계층
  서버에는 각 목적에 따른 4개의 주요 컨트롤러가 존재합니다.

---

| 컨트롤러 명칭 | 주요 기능 | 상세 설명 |
| :--- | :--- | :--- |
| **LogIngestionController** | 데이터 수집 API | 에이전트가 전송한 K8s 리소스 스냅샷을 수집하여 RabbitMQ로 전달합니다. |
| **LoginApiController** | 사용자 인증 API | ID/PW 기반 로그인을 처리하고 JWT 토큰을 발급합니다. |
| **AdminController** | 테넌트 관리 API | 신규 테넌트(고객사) 생성 및 전용 API Key 발급을 담당합니다. |
| **Policy/Alert Controller** | 정책 및 알림 API | 보안 정책 설정 조회/수정 및 탐지된 알람 내역을 대시보드에 제공합니다. |

---
  메시지 큐 (RabbitMQ) - 비동기 처리 계층
  시스템의 부하를 분산하고 실시간성을 확보하기 위해 사용됩니다.
  수집·스캔·삭제 큐를 분산 배치해 처리 책임을 나눔으로써 시스템 가용성을 높였습니다.

   * ingestion.raw.queue (수집 큐): 에이전트로부터 들어온 원본 데이터를 일시 저장합니다. 분석 엔진은 이 큐에서 데이터를 꺼내어 순차적으로 처리합니다.
   * scan.queue (스캔 큐): 특정 리소스에 대해 정밀 스캔이 필요할 때 작업 명령을 전달하는 통로로 사용됩니다.
     
---

  ### 처리 흐름 (Workflow)


  데이터 수집 및 적재
   1. 에이전트: 최초에 한번 K8s API를 통해 Pod, Service, Node 등의 상태를 수집합니다. 이후 watch api를 통해 변경사항만 실시간으로 수집합니다.
   2. 전송: 수집된 데이터를 X-API-KEY와 함께 서버의 LogIngestionController로 전송합니다.
   3. 큐잉: 서버는 데이터를 즉시 DB에 넣지 않고 RabbitMQ의 ingestion.raw.queue에 적재하여 병목 현상을 방지합니다.


  보안 분석 및 탐지 (정책 엔진)
   1. 소비: RawLogConsumer가 큐에서 데이터를 꺼냅니다.
   2. 분석: 전략 패턴(Strategy Pattern)으로 구현된 10여 가지 보안 정책을 적용합니다.
        Pod이 Privileged 모드로 실행 중인가? 최신(latest) 태그를 사용하는가? 위험 포트가 열려 있는가? 등
   3. 알림: 정책 위반 발견 시 Alert 객체를 생성하여 DB에 저장하고 대시보드에 노출합니다.


  ---
   
   
  ### 스캔 큐 vs 스케줄러의 정기 스캔

| 구분 | scan.queue (정밀 스캔) | SecurityScannerService (정기 스캔) |
| :--- | :--- | :--- |
| **성격** | 실시간/이벤트 대응 | 정기적/상태 유지 |
| **대상** | 변화가 생긴 특정한 각각의 리소스 | 전체 데이터베이스 내 리소스 |
| **목적** | 변화에 따른 즉각적인 위협 탐지 | 누락된 위협 탐지 및 데이터 정합성 보장 |
| **방식** | RabbitMQ (비동기) | Spring Scheduler + ShedLock (동기) |


  멀티 테넌시 인증 구조
   * 멀티 테넌시: 각 고객사(Tenant)별로 독립된 API Key와 정책을 관리할 수 있습니다.
   * 보안 인증: 에이전트는 API Key, 대시보드 사용자는 JWT를 사용하는 이중 보안 체계를 갖추고 있습니다.
 


---
















### opt 

---

### 1. 동기식 수집 병목 및 DB 커넥션 고갈 해결 (Message Queue & Caching 도입)

- **기존의 문제**
    - 백엔드 서버 3개 기준으로 k6 부하 테스트 시 단일 고객인 개발 테스트 환경에서는 문제 없었지만 고객사가 여러 곳 이라고 가정하여 부하 테스트를 했을 경우 각 에이전트에서 보낸 대용량 스냅샷을 서버가 동기식으로 파싱-DB저장-보안스캔까지 한 번에 처리하려다 보니, API 응답 시간이 최대 47초까지 지연되었습니다.
    - 또한 매 요청마다 API Key 인증을 위해 DB를 조회하여 커넥션 풀이 고갈되고, 결국 서버 전체의 타임아웃 장애로 이어졌습니다.
- **개선 사항 (Solution)**
    - **비동기 아키텍처 전환:** 수집 컨트롤러는 데이터를 받자마자 RabbitMQ(raw.log.queue)에 적재 후 즉시 200 OK를 반환하도록 분리했습니다.
    - **인증 로컬 캐싱:**  ConcurrentHashMap 기반 로컬 캐시를 도입하여 인증 시 발생하는 DB I/O를 0으로 만들었습니다.
- **개선 결과 (Impact)**
    - DB의 좁은 병목을 우회하여 **평균 응답 시간을 16.82초에서 289ms로 약 58배 단축**시켰으며, 대규모 부하(Throughput 133MB/2min) 상황에서도 100%의 성공률을 확보했습니다.

### 2. Event-Driven Architecture 도입 

- **기존의 문제**
    - **탐지 지연:** 1분 주기의 스케줄러 방식은 위협 발생 시 최대 **60초의 탐지 공백** 발생.
    - **리소스 낭비:** 데이터 변경이 없어도 수만 건의 DB I/O를 반복 수행하여 **서버 자원 고갈**.
    - **상태 불일치:** 리소스 삭제 이벤트 감지가 늦어 실제 클러스터와 대시보드 간 **데이터 정합성 결여**.
- **개선 사항**
    - 에이전트 기동 시 최초 1회 전수 조사를 통해 베이스라인 구축.
    - 이후 Watch API를 통해 리소스 변경(ADD, MOD, DEL)을 실시간 스트리밍
    - 전수 조사 대신, 이벤트가 발생한 특정 자산에 대해서만 즉각적인 보안 정책 엔진을 가동
    - 보안과 무관한 단순 메타데이터 변경은 해시 비교를 통해 1차 필터링하여 불필요한 스캔 차단.
- **개선 결과**
    - 실시간 탐지: 보안 위협 감지 및 알람 생성 지연 시간을 1분 -> 수 초 이내로 단축.
    - DB 부하 90% 이상 절감: 반복적인 대량 조회 쿼리를 제거하고, 이벤트 발생 시에만 선택적으로 I/O를 수행하여 시스템 가용성 극대화.
    - 완벽한 정합성 보장: 리소스 삭제 시 수 초 내에 대시보드 반영 및 관련 알람 업데이트가 이루어짐.

### 3. 분산락 적용

- **문제**
    - 여러 서버에서 스케줄러가 동시에 실행되어 스캔 작업 중복 발생
    - DB 동시 접근으로 알림 중복 및 상태 정합성 문제 발생
- **개선 (지점별 다중 방어 전략)**
    - **애플리케이션 계층:** 분산락으로 단일 인스턴스만 스캔 실행
    - **트랜잭션 계층:** 비관락을 걸어 동시 수정 차단 (SELECT ... FOR UPDATE)
    - **DB 계층:** Unique Index로 중복 데이터 삽입 방지
- **결과**
    - 중복 작업 제거 및 데이터 정합성 확보
    - 분산 환경에서 시스템 신뢰성 향상

---







---
# AI server (기능 개발중)

---

AI 서버는 쿠버네티스 보안 실시간 데이터(PostgreSQL)와 정적 보안 지식(RAG)을 융합하여 분석 결과를 제공하는 지능형 보안 에이전트입니다.

- Framework: FastAPI
- Orchestration: LangGraph
- Persistence Layer: Hybrid Strategy (Redis + PostgreSQL)

---

LangGraph 기반 워크플로우 설계
1회성 답변(Chain)이 아닌, 도구 실행 결과에 따른 재시도 및 검증(Loop)이 가능한 그래프 구조를 채택했습니다.

그래프 노드 구조

1. Router : 사용자의 질문 의도를 분석하여 db_tool 또는 rag_tool 호출을 결정합니다.
2. Tools: 실시간 K8s 자산 데이터(SQL) 또는 보안 지식베이스(RAG)를 조회합니다.
3. Validator (Self-Correction): LLM이 생성한 SQL의 문법 오류나 테넌트 필터링 누락을 감지합니다. 오류 발견 시 에러 메시지를 포함하여 다시 Agent 노드로 회귀시켜 스스로 쿼리를 수정(Self-healing)하게 유도합니다.
4. Generator: 최종 수집된 정보를 보안 컨설턴트 톤의 한국어로 요약하여 응답합니다.

---

 데이터 저장 및 맥락 유지 전략 (Persistence)
Stateless한 JWT 환경에서 대화의 맥락(Context)을 유지하기 위해 이중 저장소 사용.

 Redis: 세션 캐싱 및 실시간 상태 관리

- 현재 진행 중인 대화의 중간 상태(State)와 사용자 정보(User/Tenant Context)를 초고속으로 캐싱합니다.
- LangGraph의 상태 전파는 빈번한 읽기/쓰기가 발생하므로, 지연 시간을 최소화하기 위해 인메모리 저장소인 Redis를 선택했습니다.

PostgreSQL: PostgresCheckpointer 기반 히스토리 영구 보존

- 대화가 종료된 후에도 thread_id를 기준으로 과거 모든 대화 이력을 보존합니다.
- LangGraph의 PostgresCheckpointer를 활용해, 공유 DB 내 ai_checkpoints 테이블에 스냅샷을 저장

실시간 속도는 Redis가, 데이터의 신뢰성과 영구 보존은 PostgreSQL이 담당하도록 책임을 분리.

---

고도화된 RAG 및 인덱싱 전략
청크 전략

- 글자 수로 문서를 자를 경우, 취약점의 '설명'과 '조치 방법'이 서로 다른 청크로 나뉘어 검색 품질이 저하되는 문제 발생.
- security_kb.txt 내에 구분자를 기준으로. indexer.py에서 블록 단위로 문서를 분할하여 하나의 보안 지식이 온전한 맥락(Context)을 유지한 채 VectorDB에 저장되도록 구현.

---

 테넌트 간 데이터 격리

- 시스템 프롬프트 주입: 에이전트에게 "다중 테넌트 환경의 컨설턴트임"을 지속적으로 인지시켜, 쿼리 생성 시 스스로 필터링을 걸도록 제한.
- tenant_id = {request_tenant_id} 필터가 SQL에 포함되어 있지 않으면 실행을 즉시 차단하고 에러를 반환








