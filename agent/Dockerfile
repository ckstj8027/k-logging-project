FROM eclipse-temurin:17-jre-focal
WORKDIR /app
# Gradle 빌드로 생성된 JAR 파일을 복사합니다.
COPY build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]