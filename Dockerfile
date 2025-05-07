#FROM openjdk:17-jdk-slim
#WORKDIR /app
#COPY ./target/demo-0.0.1-SNAPSHOT.jar app.jar
#EXPOSE 8080
#ENTRYPOINT ["java", "-jar", "app.jar"]
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY ./target/demo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE $PORT
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
