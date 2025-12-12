# Multi-stage Dockerfile for User Service
# Stage 1: Build the application using Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy all source files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the service port
EXPOSE 8081

# Health check for Docker
HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
