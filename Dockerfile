# Build stage - compile the application
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# Build the application (skip tests for faster builds)
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

# Run stage - lightweight runtime image
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the application port (matches server.port in application.properties)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
