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

## 데이터 흐름

검증된 TLS 터널이 뚫린 후, 에이전트가 `리소스`를 가져오는 과정은 다음과 같은 흐름으로 진행됩니다.

1. Bearer 토큰 부착: 에이전트(Java)는 마운트된 `token` 파일의 문자열을 읽어 HTTP 헤더에 담습니다.
    - `Authorization: Bearer <JWT_TOKEN_CONTENT>`
2. API 호출 전송: TLS로 암호화된 통로를 통해 API 서버에 요청을 던집니다.
    - 예: `GET /api/v1/pods` (Pod 리스트 조회)
3. API 서버의 최종 승인: API 서버는 토큰을 보고 신원을 확인한 후, **RBAC(RoleBinding)** 설정을 확인.
    - "이 토큰의 주인(`k8s-agent-sa`)이 전체 네임스페이스의 Pod를 볼 권한 확인"
4. 데이터 응답: 권한이 확인되면 API 서버는 JSON 데이터를 응답하고, 에이전트의 `io.kubernetes:client-java`는 이를 자바 객체(`V1PodList` 등)로 변환.

6 서버로 전송 
   1. K8s API 호출 → 받은 데이터 자바 객체(`V1PodList` 등)를 Java DTO로 변환.
   2. 변환된 Java DTO들을 SnapshotBlockingQueue에 넣음.
   3. 큐에서 Java 객체를 꺼내어 JSON 문자열로 직렬화.
   4. HTTP 헤더(X-API-KEY)와 함께 서버로 전송. 

---

# Server

---

### erd 구조 

---

<img width="2754" height="2988" alt="image" src="https://github.com/user-attachments/assets/28b89252-e5e0-4e98-96d3-f569ab986a08" />

---



멀티 테넌시 구조를 기반으로, 모든 핵심 테이블(`pod_profiles`, `alerts`, `policies`, `users` 등)은 `tenant_id`를 외래 키로 사용하여 고객사별 데이터 격리를 구현했습니다.

 데이터 무결성(3NF)과 대규모 조회 성능 최적화를 동시에 만족시키기 위해 논리적 설계와 물리적 성능 설계를 분리한 하이브리드 구조를 채택했습니다.

---

### 1. Database 설계

- 전체 도메인 모델은 제3정규형(3NF) 기반으로 설계
- 테넌트 기반 SaaS 구조에서 데이터 무결성과 정합성 확보
- 주요 엔티티: `Tenants`, `Users`, `Policies`, `Alerts`
- 조회 중심 테이블(`pod_profiles`)은 성능 최적화를 위해 반정규화 적용(2NF)
- 코드 레벨에서는 `@Embeddable (AssetContext)`를 활용하여 객체지향 구조 및 재사용성 유지

---

### 2. Index 설계

모든 테이블은 tenant_id 기반 멀티 테넌시 구조로 설계되어 데이터 격리를 보장합니다.

#### 주요 인덱스 전략

- `tenant_id` 기반 데이터 분리 (테넌트 격리)
- `last_seen_at` 기반 정렬 성능 최적화
- 복합 인덱스를 통한 조회 성능 최적화

#### 인덱스 적용 예시 (`pod_profiles`)

- `(tenant_id, namespace, pod_name, container_name)` → 데이터 식별 및 조회 최적화
- `last_seen_at` → 최신 데이터 조회 최적화

---

### 3. 쿼리 최적화

대규모 데이터 환경에서의 조회 성능을 위해 다음과 같은 최적화를 적용했습니다.

#### 3.1 No-Offset Pagination

- `OFFSET` 기반 페이징의 성능 저하 문제 해결
- `lastId 기반 Keyset Pagination` 적용
- 페이지 위치와 무관하게 일정한 성능 유지

---

#### 3.2 Index Range Scan

- `tenant_id` 기반 복합 인덱스 활용
- Full Table Scan 제거
- 테넌트 단위 조회 성능을 데이터 규모와 무관하게 유지

---

#### 3.3 Zero-Join Architecture

- `pod_profiles`에 `AssetContext`를 `@Embedded`로 통합하여 반정규화 적용
- 자주 조회되는 자산 정보를 단일 테이블로 구성
- JOIN 제거를 통한 I/O 최소화

---

#### 3.4 Sorting Optimization

- `last_seen_at` 기반 인덱스 정렬 구조 설계
- 별도의 Sort 없이 Index Scan만으로 정렬 처리

---

### 4. 결과

- 3NF 기반 데이터 무결성 확보
- 선택적 반정규화로 조회 성능 최적화 (JOIN 비용 제거)
- Covering Index로 테이블 접근 제거
- Index Range Scan+Keyset Pagination + Index Sorting 결합으로 페이징/정렬 병목 제거
- 멀티 테넌시 기반 SaaS 확장 구조 지원

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




