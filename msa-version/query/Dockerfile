FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY application/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
