# 1. Build Stage
FROM gradle:8.5-jdk17 AS builder
WORKDIR /project

# Gradle Wrapper 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 모듈별 build.gradle 복사
COPY server/build.gradle server/
COPY agent/build.gradle agent/

# 전체 소스 복사
COPY . .

# Agent 모듈 빌드
RUN ./gradlew :agent:bootJar -x test --no-daemon

# 2. Run Stage
FROM openjdk:17.0.2
WORKDIR /app

# 빌드된 Jar 파일 복사 (경로 주의: agent/build/libs/...)
COPY --from=builder /project/agent/build/libs/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]