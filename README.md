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


<img width="2814" height="1536" alt="ar" src="https://github.com/user-attachments/assets/63871201-aca7-48ae-bb4f-66a25b6814af" />

시스템은 크게 에이전트(Agent)와 관리 서버(Server)로 나뉘며, 메시지 브로커(RabbitMQ)와 관계형 데이터베이스(PostgreSQL)를 중심으로 동작합니다.

* Agent (Client Side): K8s 클러스터 내부 리소스를 수집하여 서버로 전송합니다.
* Backend Server: 수집된 데이터를 분석하고, 정책에 따라 보안 위협을 탐지하며 알람을 생성합니다.
  (Data Flow: 수집(Blue Flow) → 분석 및 알림(Yellow Flow) → 데이터 정리(Green Flow)의 순환 구조를 가집니다)
* AI Server: 수집된 실시간 자산 데이터와 보안 지식베이스(RAG)를 융합 분석하여, 탐지된 위협에 대한 지능형 조치 가이드를 제공하는 보안 의사결정 지원 엔진


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

- 서버의 증명: K8s API 서버는 에이전트에게 자신의 인증서을 보여줍니다.
- (Cluster CA가 보증하는 API 서버의 공개키)
- 검증: 에이전트는 마운트된 `ca.crt` 도장과 서버가 보여준 신분증의 도장을 대조합니다.
    - 도장이 맞으면: `ApiClient`가 "오케이, 진짜 서버네!" 하고 통신을 시작합니다.
    - 도장이 틀리면: Java에서 `SSLHandshakeException` 에러가 나면서 통신이 끊깁니다.

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
   3. 큐에서 Java 객체를 꺼내어 JSON 문자열로 직렬화(Jackson).
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

이미지 하단의 shedlock 테이블은 프로젝트의 DataInitializer 클래스에서 실제 SQL로 생성. 분산 환경에서의 스케줄링 중복 방지 설계

 
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
   스케줄러 (Scheduler) - 자동화 계층
  ShedLock을 활용하여 분산 환경에서도 중복 없이 정기적인 작업을 수행합니다.


   ResourceCleanupService (데이터 정리): 매 1분마다 실행됩니다. 에이전트로부터 2분 이상 보고가 없는(죽은) 리소스를 DB에서 자동으로 삭제하여 최신 상태를 유지합니다.
   SecurityScannerService (정기 스캔): 매 1시간마다 실행됩니다. 실시간 탐지에서 놓칠 수 있는 위협을 위해 모든 테넌트의 리소스를 전수 조사합니다.

  ---

  핵심 처리 흐름 (Workflow)


  [Blue Flow] 데이터 수집 및 적재
   1. 에이전트: K8s API를 통해 Pod, Service, Node 등의 상태를 1분 주기로 수집합니다.
   2. 전송: 수집된 데이터를 X-API-KEY와 함께 서버의 LogIngestionController로 전송합니다.
   3. 큐잉: 서버는 데이터를 즉시 DB에 넣지 않고 RabbitMQ의 ingestion.raw.queue에 적재하여 병목 현상을 방지합니다.


  [Yellow Flow] 보안 분석 및 탐지 (정책 엔진)
   1. 소비: RawLogConsumer가 큐에서 데이터를 꺼냅니다.
   2. 분석: 전략 패턴(Strategy Pattern)으로 구현된 10여 가지 보안 정책을 적용합니다.
        Pod이 Privileged 모드로 실행 중인가? 최신(latest) 태그를 사용하는가? 위험 포트가 열려 있는가? 등
   3. 알림: 정책 위반 발견 시 Alert 객체를 생성하여 DB에 저장하고 대시보드에 노출합니다.


  [Green Flow] 시스템 유지 관리
   1. 감시: 스케줄러가 리소스의 lastSeenAt 시간을 체크합니다.
   2. 삭제: 비정상 종료된 에이전트의 잔적이나 오래된 로그 데이터를 삭제하여 DB 성능을 최적화합니다.

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
















opt
---
---

### 1. 동기식 수집 병목 및 DB 커넥션 고갈 해결 (Message Queue & Caching 도입)

- **기존의 문제**
    - 단일 고객인 개발 테스트 환경에서는 문제 없었지만 고객사가 여러 곳 이라고 가정하여 부하 테스트를 했을 경우 각 에이전트에서 보낸 대용량 스냅샷(JSON)을 서버가 동기식(Synchronous)으로 파싱-DB저장-보안스캔까지 한 번에 처리하려다 보니, API 응답 시간이 최대 47초까지 지연되었습니다.
    - 또한 매 요청마다 API Key 인증을 위해 DB를 조회하여 커넥션 풀이 고갈되고, 결국 서버 전체의 타임아웃 장애로 이어졌습니다.
- **개선 사항 (Solution)**
    - **비동기 아키텍처 전환:** 수집 컨트롤러는 데이터를 받자마자 RabbitMQ(raw.log.queue)에 적재 후 즉시 200 OK를 반환하도록 분리했습니다.
    - **인증 로컬 캐싱:**  ConcurrentHashMap 기반 로컬 캐시를 도입하여 인증 시 발생하는 DB I/O를 0으로 만들었습니다.
- **개선 결과 (Impact)**
    - DB의 좁은 병목을 우회하여 **평균 응답 시간을 16.82초에서 289ms로 약 58배 단축**시켰으며, 대규모 부하(Throughput 133MB/2min) 상황에서도 100%의 성공률을 확보했습니다.

### 2. 무의미한 DB I/O의 극단적 최적화 (Fingerprint Hash & Smart Merge)

- **기존의 문제**
    - 1분마다 들어오는 데이터 중 변경된 것이 없어도 엔티티 변환이 발생했고, 특히 Service Port 같은 컬렉션은 매번 전체 삭제 후 재삽입(clear() -> addAll())되는 최악의 비효율(Delete-Insert N+1)이 발생했습니다.
- **개선 사항**
    - **Fingerprint(해시) 기반 변경 감지:** 보안에 직결된 핵심 필드만 조합해 SHA-256 해시를 생성. 에이전트의 데이터와 DB의 해시값이 일치하면 업데이트 로직 자체를 Skip 하도록 애플리케이션 레벨에서 차단했습니다.
    - **생존 신고(Heartbeat) 최적화:** 데이터 변화가 없을 시 lastSeenAt 업데이트 주기를 1분에서 5분으로 늘려 쓰기 빈도를 줄였습니다.
    - **Smart Collection Merge:** 포트 정보 갱신 시 removeIf와 조건부 add를 사용하여 실제 변경된 포트만 DB에 반영하도록 JPA 로직을 정밀 튜닝했습니다.
- **개선 결과**
    - 1,000개의 스냅샷이 들어와도 실제 변경된 2~3개에서만 Dirty Checking이 발생하여 **불필요한 DB Write 부하를 99% 이상 절감**했습니다.

### 3. 하드코딩된 보안 정책의 객체지향적 재설계 (Policy Engine Pattern)

- **기존의 문제**
    - 보안 정책들이 단순 if-else문으로 작성되어 있어 가독성이 최악이었고, 새로운 정책을 추가할 때마다 기존 코드를 수정해야 하는 **OCP(개방-폐쇄 원칙) 위반** 상태였습니다. 또한, 모든 자산이 무의미하게 전체 정책을 순회해야 했습니다.
- **개선 사항**
    - Policy 인터페이스를 정의하고 각 정책을 독립된 클래스로 분리하는 Strategy Pattern **패턴**을 도입했습니다.
    - 정책에 AssetType 기반의 **Policy Scope** 개념을 적용하여, Map<AssetType, List<Policy>> 형태로 메모리에 그룹핑했습니다.
- **개선 결과**
    - 기존에 모든 타입에 관계없이 리소스들을 모든 정책을 다 확인하는 방식 → 자산 타입에 맞는 정책만 선택적으로 평가하게 되어 평가 성능이 대폭 향상되었고, **기존 코드의 수정 없이 새로운 정책 클래스만 추가하면 되는  확장성**을 확보했습니다.

### 4. 풀 스캔에서 '실시간 이벤트 기반 핀포인트 스캔'으로

- **기존의 문제**
    - 1분 주기의 스케줄러가 수만 개의 전체 자산을 DB에서 긁어와(Full Scan) 보안 위협을 검사했습니다. 감지까지 최대 1분이 걸렸으며, 서버가 여러 대일 경우 동일한 작업을 중복 실행하는 문제가 발생했습니다.
- **개선 사항**
    - **이벤트 기반 정밀 스캔:** Fingerprint 비교 후 '실제로 데이터가 변경된 자산의 ID'만 추출하여 스캔 이벤트 큐(processed.event.queue)에 발행(Publish)하도록 아키텍처를 변경했습니다.
    - **분산 환경 정합성:** 서버가 여러대 일경우 각 서버마다 스케줄러가 실행되는데, 스케줄러 중복 방지를 위해 ShedLock을 도입하고, DB 레벨에 Unique Index를 걸어 알림 중복 생성을 원천 차단했습니다.
- **개선 결과**
    - 스캔 대상이 '테넌트 전체 자산'에서 '변경된 극소수 자산'으로 **99.9% 감소**했으며, 위협 감지 속도가 스케줄러 대기 없이 **1초 이내(즉시)로 약 50배 향상**되었습니다.

---

### 성능 및 구조 개선

- **No-Offset 페이징 적용**  
  대량 데이터 환경에서 Offset 기반 페이징의 성능 저하 문제를 해결하기 위해 Querydsl을 활용한 Cursor(No-Offset) 방식의 페이징을 도입했습니다. 이를 통해 데이터가 수만 건 이상 증가하더라도 조회 응답 시간을 **약 100ms 수준으로 안정적으로 유지**하도록 개선했습니다.

- **도메인 엔티티 공통 필드 추출**  
  여러 엔티티에 분산되어 있던 공통 필드(`Tenant`, `createdAt` 등)를 `@MappedSuperclass` 기반의 부모 클래스(`BaseResourceProfile`)로 추상화하여 중복 코드를 제거하고 도메인 모델의 **일관성과 유지보수성을 향상**시켰습니다.












