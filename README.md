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



