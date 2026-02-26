
자산 평가 전 상황 
<img width="1893" height="526" alt="화면 캡처 2026-02-26 193310" src="https://github.com/user-attachments/assets/14cbd039-82c2-4928-8b8d-4ed32da7a89d" />


agent 가 리소스를 수집하여 서버로 보내 처리 

<img width="1907" height="502" alt="화면 캡처 2026-02-26 193428" src="https://github.com/user-attachments/assets/52b11182-3f79-467f-9772-768e1acd6db7" />

pod, service, alerts 세부 정보 확인가능 

<img width="1878" height="808" alt="화면 캡처 2026-02-26 193453" src="https://github.com/user-attachments/assets/2094a3e2-d5fc-49ca-8a61-640c8eeaa041" />


<img width="1862" height="813" alt="화면 캡처 2026-02-26 193507" src="https://github.com/user-attachments/assets/0ce6cba8-bf72-4d1d-b95e-9140c77f938a" />


<img width="1900" height="806" alt="화면 캡처 2026-02-26 193526" src="https://github.com/user-attachments/assets/7f882197-6aaa-4889-84d0-773abd3be79e" />


위험한 pod 배포 
<img width="450" height="122" alt="화면 캡처 2026-02-26 193600" src="https://github.com/user-attachments/assets/6bf2d307-dee3-4c07-914f-4a4676f47d0e" />

<img width="1557" height="345" alt="화면 캡처 2026-02-26 193702" src="https://github.com/user-attachments/assets/75dda21a-f1b8-4960-9263-f45e3487ad49" />

조금 기다리면 경고 생성 
<img width="1387" height="67" alt="화면 캡처 2026-02-26 193721" src="https://github.com/user-attachments/assets/3f60c6f1-6ef5-47b7-ad23-1ad11f1e2091" />

<img width="1898" height="493" alt="화면 캡처 2026-02-26 193740" src="https://github.com/user-attachments/assets/0ab6f5d2-cfb4-40a0-9109-e4d98ed9cc24" />


알람은 사전에 정의한 규칙에 의해 생김 

root user 및 priviledged 모드 , 그리고 default ns 에 생성되었다는 위험 알람이 생김 
<img width="1527" height="743" alt="화면 캡처 2026-02-26 193800" src="https://github.com/user-attachments/assets/d5bacdc8-0ed3-4b65-9d81-ec55421d1a2e" />


확인후 문제되는 pod 를 삭제 

<img width="775" height="312" alt="화면 캡처 2026-02-26 194106" src="https://github.com/user-attachments/assets/7945132a-a9de-4686-ab99-83c84db44e60" />


기존에는 ON DELETE CASCADE 옵션을 주어 Pod이 삭제될 때 해당 Pod에 걸려있던 모든 Alert도 흔적 없이 사라지게 했었는데 
"방금 전까지 해킹 위협 알람이 떠 있었는데, Pod이 삭제되자마자 증거가 사라지는" 꼴이 되어
보안 솔루션에서는 이게 꽤 치명적일 수 있다고 판단하여 기록은 게속 남기는 방향으로 진행 
보안 위협 의삼되는 pod를 삭제함으로서 31->30 은 즉시 반영 
<img width="1891" height="416" alt="화면 캡처 2026-02-26 194045" src="https://github.com/user-attachments/assets/b49613bc-cd34-4f69-a296-b3f124d40d5b" />




자산 평가는 agent 가 주기적으로 스케줄링을 통해 kuber-api 호출을 통해 얻은 리소스 정보를 서버에 보내는 방식으로 구동 


<img width="1691" height="887" alt="화면 캡처 2026-02-26 194259" src="https://github.com/user-attachments/assets/fdda3d91-28a5-4b32-a4c5-a054f6a716b3" />


















