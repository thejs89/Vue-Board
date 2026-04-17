FROM eclipse-temurin:11-jre-alpine
WORKDIR /app

COPY board-app/build/libs/board-app.jar board-app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
