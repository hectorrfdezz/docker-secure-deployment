## Backend Dockerfile
#
# Builds the Spring Boot application using a multi‑stage process.  The
# first stage uses Maven to compile the application into an executable
# jar.  The second stage runs the jar inside a minimal Java runtime and
# drops privileges to a non‑root user for better security.  If you need
# to install additional runtime dependencies they should be added to
# the final stage rather than the build stage.

# Stage 1: build the application
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests

# Stage 2: runtime image
FROM eclipse-temurin:17-jre-alpine

# Create a dedicated unprivileged user to run the application.  The UID
# and GID (1000) align with typical host systems but may be tuned later.
RUN adduser -D -u 1000 appuser
USER appuser
WORKDIR /home/appuser

# Copy the built jar from the build stage.  The jar name is derived
# from the Maven artifactId and version defined in pom.xml.
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Expose the default Spring Boot port.  This does not publish the port
# outside the container but documents the service for introspection tools.
EXPOSE 8080

# Run the application.  Additional JVM arguments can be provided here
# (e.g. to tune heap size) but are omitted for simplicity.
ENTRYPOINT ["java","-jar","/home/appuser/app.jar"]