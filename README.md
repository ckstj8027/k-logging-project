대규모 K8s 자산 수집 및 실시간 보안 관리 플랫폼
----------------------------------------------------------------------------------------------------------------------------------
프로젝트 개요
K-Sentry는 쿠버네티스 클러스터 내의 수만 개 자산(Pod, Service 등)을 에이전트 기반으로 실시간 수집하고 
보안 취약점(Privileged 권한 탈취, 블랙리스트 포트 노출 등)을 진단하는 SaaS형 클라우드 보안 플랫폼입니다.



#
회원가입 후 발급 받은 api key로 본인의 쿠버환경에 에이전트를 배포 


![5](https://github.com/user-attachments/assets/04427b83-bae5-495b-9233-15ee06f28d6e)

#
에이전트가 리소스들을 서버에 보내고 서버에서 처리하는 모습 

![6](https://github.com/user-attachments/assets/ba7cd5c1-b720-42e5-be5b-6dcd492f3a63)

#
쿠버환경에 root user 및 Privilege Mode로 실행되는 pod 를 실행한 이후 ->정책에 의해 탐지되는 모습 

![2](https://github.com/user-attachments/assets/9a9e98a2-0511-4452-8eed-791d893084fe)

#서버에서 정책 중에 replica 최대 제한 수 10->2로 바꾼 후 쿠버에서는 deployment 의 replica 수를 1 -> 3 으로 늘린다 -> 정책에 의해 탐지되는 모습 

![3](https://github.com/user-attachments/assets/6edde7b1-d226-4d25-9efa-695fda6f5756)

#




System Architecture
----------------------------------------------------------------------------------------------------------------------------------
에이전트와 서버 간의 데이터 흐름 및 동기화 아키텍처입니다.



<img width="3136" height="1344" alt="Gemini_Generated_Image_h4ta55h4ta55h4ta" src="https://github.com/user-attachments/assets/094f6487-7b19-431a-8512-926392d9ba60" />

Agent: 각 클러스터에서 K8s API 서버로부터 리소스 정보를 수집하여 JSON 스냅샷 전송.

Server: 수신된 데이터를 파싱하고 식별자를 추출하여 기존 데이터와 대조 , 쿠버네티스 리소스 처리 및 보안 alerts 생성 





opt
----------------------------------------------------------------------------------------------------------------------------------

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

### 4. 무식한 전수 스캔에서 '실시간 이벤트 기반 핀포인트 스캔'으로

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


















Demo 
----------------------------------------------------------------------------------------------------------------------------------
자산 평가 전 상황 
<img width="1893" height="526" alt="화면 캡처 2026-02-26 193310" src="https://github.com/user-attachments/assets/14cbd039-82c2-4928-8b8d-4ed32da7a89d" />


agent 가 리소스를 수집하여 서버로 보내 처리 

<img width="1907" height="502" alt="화면 캡처 2026-02-26 193428" src="https://github.com/user-attachments/assets/52b11182-3f79-467f-9772-768e1acd6db7" />

pod, service, alerts 세부 정보 확인가능 

<img width="1878" height="808" alt="화면 캡처 2026-02-26 193453" src="https://github.com/user-attachments/assets/2094a3e2-d5fc-49ca-8a61-640c8eeaa041" />


<img width="1862" height="813" alt="화면 캡처 2026-02-26 193507" src="https://github.com/user-attachments/assets/0ce6cba8-bf72-4d1d-b95e-9140c77f938a" />


<img width="1900" height="806" alt="화면 캡처 2026-02-26 193526" src="https://github.com/user-attachments/assets/7f882197-6aaa-4889-84d0-773abd3be79e" />




위험한 pod 배포 
<img width="950" height="345" alt="화면 캡처 2026-02-26 193600" src="https://github.com/user-attachments/assets/6bf2d307-dee3-4c07-914f-4a4676f47d0e" />

리소스도 정상 반영 됨
<img width="1557" height="345" alt="화면 캡처 2026-02-26 193702" src="https://github.com/user-attachments/assets/75dda21a-f1b8-4960-9263-f45e3487ad49" />


 
 alerts 스케줄러가 주기적으로 db를 스캔해 사전에 정의한 위험 체크리스트에 해당하는걸 발견하면 alert 생성 
<img width="1387" height="67" alt="화면 캡처 2026-02-26 193721" src="https://github.com/user-attachments/assets/3f60c6f1-6ef5-47b7-ad23-1ad11f1e2091" />

<img width="1898" height="493" alt="화면 캡처 2026-02-26 193740" src="https://github.com/user-attachments/assets/0ab6f5d2-cfb4-40a0-9109-e4d98ed9cc24" />


3개가 생성됨이 보임 

root user 및 priviledged 모드 , 그리고 default ns 에 생성되었다는 위험 알람이 생김 
<img width="829" height="398" alt="image" src="https://github.com/user-attachments/assets/ae2368a3-f95f-44e5-bd90-1d4833c5b31c" />




확인후 문제되는 pod 를 삭제 

<img width="775" height="312" alt="화면 캡처 2026-02-26 194106" src="https://github.com/user-attachments/assets/7945132a-a9de-4686-ab99-83c84db44e60" />


기존에는 ON DELETE CASCADE 옵션을 주어 Pod이 삭제될 때 해당 Pod에 걸려있던 모든 Alert도 흔적 없이 사라지게 했었는데 

"방금 전까지 해킹 위협 알람이 떠 있었는데, Pod이 삭제되자마자 증거가 사라지는" 꼴이 되어

보안 솔루션에서는 이게 꽤 치명적일 수 있다고 판단하여 기록은 게속 남기는 방향으로 진행 

보안 위협 의삼되는 pod를 삭제함으로서 31->30 은 즉시 반영 
<img width="1891" height="416" alt="화면 캡처 2026-02-26 194045" src="https://github.com/user-attachments/assets/b49613bc-cd34-4f69-a296-b3f124d40d5b" />




자산 평가는 agent 가 주기적으로 스케줄링을 통해 kuber-api 호출을 통해 얻은 리소스 정보를 서버에 보내는 방식으로 구동 


<img width="1691" height="887" alt="화면 캡처 2026-02-26 194259" src="https://github.com/user-attachments/assets/fdda3d91-28a5-4b32-a4c5-a054f6a716b3" />


















