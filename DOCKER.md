# Docker Setup

This document explains how to containerize and run the Guestbook application with PostgreSQL using Docker.

## Files

1. **Dockerfile** - Multi-stage build for the Spring Boot application
2. **docker-compose.yml** - Orchestrates the app, PostgreSQL, and pgAdmin
3. **.dockerignore** - Excludes unnecessary files from the build context

## Quick Start

```bash
docker-compose up --build
```

This starts three services:
- **guestbook-app** - The Spring Boot application on port 8080
- **guestbook-postgres** - PostgreSQL database on port 5432
- **guestbook-pgadmin** - pgAdmin web UI on port 5050

## Accessing Services

| Service | URL | Credentials |
|---------|-----|-------------|
| Application | http://localhost:8080 | - |
| pgAdmin | http://localhost:5050 | admin@admin.com / admin |

### Connecting pgAdmin to PostgreSQL

1. Open http://localhost:5050
2. Login with admin@admin.com / admin
3. Right-click "Servers" > "Register" > "Server"
4. General tab: Name = `guestbook`
5. Connection tab:
   - Host: `postgres` (Docker service name)
   - Port: `5432`
   - Database: `guestbook_db`
   - Username: `alex`
   - Password: `pcbe`

## How It Works

### Dockerfile (Multi-stage build)

**Stage 1 (builder):** Compiles the application using JDK 21.
- Copies Gradle wrapper and source files
- Runs `./gradlew bootJar` to create an executable JAR

**Stage 2 (runtime):** Runs the application using JRE 21.
- Copies only the compiled JAR
- Exposes port 8080

### docker-compose.yml

The compose file defines three services:

1. **postgres** - PostgreSQL 16 with persistent volume
   - Healthcheck ensures database is ready before app starts
   
2. **pgadmin** - Web-based database management tool

3. **app** - The Spring Boot application
   - Waits for PostgreSQL healthcheck
   - Environment variables override `application.properties` for container networking

## Commands

```bash
# Start all services
docker-compose up --build

# Start in background
docker-compose up -d --build

# View logs
docker-compose logs -f app

# Stop all services
docker-compose down

# Stop and remove volumes (deletes database data)
docker-compose down -v
```

## Test the API

```bash
# Create an entry
curl -X POST http://localhost:8080/guestbook \
  -H "Content-Type: application/json" \
  -d '{"name":"Docker User","message":"Hello from container!"}'

# List entries
curl http://localhost:8080/guestbook
```

## Notes

- Database data persists in a Docker volume (`postgres_data`)
- Use `docker-compose down -v` to reset the database
- The app connects to PostgreSQL using the Docker service name (`postgres`) as hostname
