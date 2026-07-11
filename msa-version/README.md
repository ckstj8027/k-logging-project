# msa-version — 헥사고날 멀티모듈 아키텍처

`hexagonal-module-sample-main`(flex 샘플)의 규칙을 Java 17 / Spring Boot 3.4.2 환경에 맞게 적용한 구조다.
핵심 아이디어: **모듈 경계를 Gradle 서브모듈로 물리적으로 강제**하고, 결합은 `@ComponentScan`이 아니라
**Spring Boot AutoConfiguration import 메커니즘**으로 수행한다.

## 서비스(도메인) 구성

| 서비스 | 역할 | 포트 |
|---|---|---|
| `ingestion` | 에이전트 수집 수신 → Kafka 발행 | 8081 |
| `analysis` | Kafka 소비 → 보안 정책 평가 → Alert 생성 | 8082 |
| `query` | 대시보드/자산/알림 조회 + 정책 수정 | 8083 |
| `auth` | 회원가입/로그인/JWT 발급 | 8084 |
| `common` | 공유 커널 (enum, Kafka 메시지 DTO, 공통 Jackson) | - |

## 서비스별 모듈 구조와 헥사고날 역할

| 모듈 | 헥사고날 역할 | 내용 |
|---|---|---|
| `model` | Domain | 순수 도메인 모델(POJO). **프레임워크 의존성 금지** |
| `exception` | Domain | 도메인 예외 |
| `infrastructure` | Out-Port | 외부 세계로 나가는 규격(인터페이스)만. **구현 없음** |
| `service` | UseCase (In-Port) | 유스케이스 인터페이스 + package-private 구현 |
| `repository-jpa` | Out-Adapter | JPA 엔티티 + Spring Data 리포지토리 + 포트 구현 |
| `cache-redis` | Out-Adapter | Redis 캐시 포트 구현 (analysis, query) |
| `messaging-kafka` | Out-Adapter | Kafka 발행 포트 구현 (ingestion) |
| `consumer-kafka` | In-Adapter | Kafka 리스너 + k8s SDK 파싱 (analysis 전용) |
| `api` | In-Adapter | REST 컨트롤러 + Security 설정 |
| `application` | Bootstrap | main 클래스 + application.yml. **조립 전용** |

## 의존 방향 규칙 (Gradle이 컴파일 타임에 강제)

| 모듈 | 허용 의존성 |
|---|---|
| model | 없음 (common:model 만 예외) |
| exception | 없음 |
| infrastructure | model |
| service | model, exception, infrastructure |
| repository-jpa / cache-redis / messaging-kafka | model, infrastructure |
| consumer-kafka | model, service |
| api | model, exception, service |
| application | api + 어댑터 전부 |

**금지 규칙**
- `api` → `repository-jpa` 직접 참조 금지 (반드시 service 경유)
- 서비스(도메인) 간 직접 참조 금지 — 통신은 Kafka/REST 계약(common:model)으로만
- `model`/`service`/`infrastructure`에 jakarta.persistence, io.kubernetes, Redis/Kafka 타입 금지

규칙 위반은 build.gradle에 의존성이 없으면 컴파일이 실패하는 방식으로 물리적으로 차단된다.
필요한 의존성을 추가하고 싶어지면 그것이 아키텍처 위반 신호다.

## 조립 방식 (No ComponentScan)

각 모듈은 자신의 빈을 `@AutoConfiguration` 클래스 + 
`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`로 노출한다.

- 유스케이스/어댑터 구현체는 package-private, 인터페이스 타입으로만 `@Bean` 노출
- 컨트롤러는 `@Import(XxxController.class)` 방식으로 등록
- JPA는 어댑터 모듈의 AutoConfiguration에 `@EntityScan` + `@EnableJpaRepositories(basePackageClasses=...)`
- `application` 모듈의 main 클래스는 `<서비스>.application` 패키지에 있어 다른 패키지를 스캔하지 않음

## 빌드 컨벤션 (buildSrc)

샘플의 type 토큰(gradle.properties)을 buildSrc 컨벤션 플러그인 ID로 구현했다.

| 플러그인 | 용도 | 샘플의 대응 타입 |
|---|---|---|
| `cnapp.java` | 공통(Java 17, Lombok, Boot BOM, JUnit) | `kotlin` |
| `cnapp.java-library` | 순수 라이브러리 (model, exception, infrastructure) | `kotlin-lib` |
| `cnapp.java-boot` | Spring 컨텍스트 필요 모듈 | `kotlin-boot` |
| `cnapp.java-boot-mvc` | REST In-Adapter | `kotlin-boot-mvc` |
| `cnapp.java-boot-jpa` | JPA Out-Adapter | `kotlin-boot-jdbc-repository` |
| `cnapp.java` + `apply plugin: 'org.springframework.boot'` | 실행 모듈 | `kotlin-boot-*-application` |

각 모듈의 build.gradle에는 **컨벤션 플러그인 ID와 모듈 간 의존성만** 선언한다.

## 빌드 / 배포

```
# 개별 서비스 빌드 (리포 루트에서)
./gradlew :msa-version:ingestion:application:bootJar

# 전체 이미지 빌드+푸시 (리포 루트에서)
./build-msa-images.ps1
```

Dockerfile은 각 서비스 루트(`ingestion/Dockerfile` 등)에 있고 `application/build/libs/*.jar`를 복사한다.
Helm 차트(`msa-infra/helm`)는 이미지 이름 기준이라 변경 없음.

## 샘플과 다른 점 (의도된 결정)

- Kotlin → Java 17 유지, LINE build-recipe → buildSrc 컨벤션 플러그인 (동일 철학)
- Spring Data JDBC + Liquibase → JPA + `ddl-auto: update` 유지 (기존 DB 동작 보존). `schema` 모듈은 Liquibase 도입 시점에 추가 권장
- 전역 ObjectMapper 동작 보존을 위해 `common:jackson` 모듈 유지 (analysis는 IntOrString 직렬화가 필요해 `consumer-kafka`의 AutoConfiguration이 대체)
