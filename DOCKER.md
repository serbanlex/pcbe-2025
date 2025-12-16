# Docker Setup

This document explains how to containerize and run the Guestbook application using Docker.

## Files Added

1. **Dockerfile** - Multi-stage build configuration
2. **.dockerignore** - Excludes unnecessary files from the Docker build context

## How the Dockerfile Works

The Dockerfile uses a multi-stage build:

**Stage 1 (builder):** Uses a full JDK image to compile the application.
- Copies Gradle wrapper and source files
- Runs `./gradlew bootJar` to create an executable JAR

**Stage 2 (runtime):** Uses a lightweight JRE image for the final container.
- Copies only the compiled JAR from the builder stage
- Exposes port 8080 (matching `server.port` in `application.properties`)
- Runs the application with `java -jar app.jar`

This approach keeps the final image small since it does not include the JDK or build tools.

## Usage

### Build the image

```bash
docker build -t guestbook-dummy-app .
```

### Run the container

```bash
docker run -p 8080:8080 guestbook-dummy-app --name guestbook-dummy-app-container
```

The application will be available at http://localhost:8080

### Test the API

```bash
# Create an entry
curl -X POST http://localhost:8080/guestbook \
  -H "Content-Type: application/json" \
  -d '{"name":"Docker User","message":"Hello from container!"}'

# List entries
curl http://localhost:8080/guestbook
```

## Notes

- The container port must match `server.port=8080` in `application.properties`
- Data is stored in memory and will be lost when the container stops
