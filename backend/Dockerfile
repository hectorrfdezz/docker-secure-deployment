## Backend Dockerfile
# Multi-stage build for the Spring Boot backend.
# Stage 1 builds the application with Maven.
# Stage 2 runs the final JAR with a non-root user and includes curl for healthchecks.

FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:17-jre-alpine

# curl is required by the docker-compose healthcheck.
RUN apk add --no-cache curl \
    && adduser -D -u 1000 appuser

USER appuser
WORKDIR /home/appuser

COPY --from=builder --chown=appuser:appuser /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/home/appuser/app.jar"]