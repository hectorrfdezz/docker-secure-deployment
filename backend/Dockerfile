# Backend Dockerfile
# Multi-stage build for the Spring Boot backend.

FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:17-jre-alpine

# curl is required by the Docker Compose healthcheck.
RUN apk add --no-cache curl \
    && addgroup -S appgroup \
    && adduser -S -u 1000 -G appgroup appuser

WORKDIR /home/appuser
COPY --from=builder /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/home/appuser/app.jar"]
