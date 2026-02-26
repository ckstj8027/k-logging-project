K8S-CNAPP Log Project
----------------------------------------------------------------------------------------------------------------------------------
쿠버네티스 클러스터의 실시간 상태를 수집하고, 리소스의 라이프사이클(생성·수정·삭제)을 추적하여 보안 가시성을 제공하는 백엔드 엔진입니다.

Cluster Snapshot Sync: 에이전트로부터 전달받은 리소스 스냅샷을 기반으로 DB 상태 최적화 동기화.

Resource Lifecycle Tracking: 리소스 삭제 시 NOT IN 쿼리를 활용한 효율적인 데이터 정리 및 정합성 유지.

Memory Efficiency: 수만 개의 리소스를 처리할 때 메모리 부하를 방지하기 위한 식별자(Key) 기반 벌크 업데이트.

Security Context Auditing: Pod 및 컨네이너의 보안 설정(Privileged, User ID 등) 자동 추출 및 모니터링.


opt
----------------------------------------------------------------------------------------------------------------------------------
Dirty Checking과 Batch Insert를 결합하여 DB I/O를 최소화

Update: 불필요한 Update 쿼리를 방지하기 위해 JPA의 Dirty Checking에 의존하여 변경된 자산만 선택적으로 반영

Insert: 네트워크 오버헤드를 줄이기 위해 신규 자산은 건별 저장이 아닌 saveAll()을 통한 벌크 연산으로 처리



System Architecture
----------------------------------------------------------------------------------------------------------------------------------
에이전트와 서버 간의 데이터 흐름 및 동기화 아키텍처입니다.



<img width="3136" height="1344" alt="Gemini_Generated_Image_h4ta55h4ta55h4ta" src="https://github.com/user-attachments/assets/094f6487-7b19-431a-8512-926392d9ba60" />

Agent: 각 클러스터에서 K8s API 서버로부터 리소스 정보를 수집하여 JSON 스냅샷 전송.

Server: 수신된 데이터를 파싱하고 식별자를 추출하여 기존 데이터와 대조 , 쿠버네티스 리소스 처리 및 보안 alerts 생성 




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


















