# Demo Guestbook Service

## Purpose
A simple Spring Boot application demonstrating a layered architecture (domain, application/service, infrastructure, controllers) for a Guestbook feature. Users can sign the guestbook with their name and a message. The service layer depends only on the `GuestbookRepository` interface, enabling easy swapping of persistence implementations.

## Current State
- **Active repository implementation**: PostgreSQL (`PostgresGuestbookRepository`) using Spring Data JPA with Hibernate - marked as `@Primary`
  - Uses `JpaGuestbookRepository` (extends `JpaRepository<GuestbookEntryEntity, String>`)
  - Entity-Domain mapping handled by `GuestbookEntryMapper`
  - Automatic database initialization via `DatabaseInitializer` (creates `guestbook_db` if missing)
  - Hibernate auto-generates tables based on `GuestbookEntryEntity`
- Alternative implementation: In-memory (`InMemoryGuestbookRepository`) using a ConcurrentHashMap - available but not active
- Domain model: `GuestbookEntry` (id, name, message, createdAt)
- See [POSTGRESQL_SETUP.md](docs/POSTGRESQL_SETUP.md) for PostgreSQL setup instructions

## API Endpoints
1. GET `/guestbook` – Retrieve all guestbook entries.
2. POST `/guestbook` – Create a new entry.

### Request (POST /guestbook)
Content-Type: application/json
```json
{
  "name": "Alice",
  "message": "Great place!"
}
```
### Response Example
```json
{
  "id": "4f6d...",
  "name": "Alice",
  "message": "Great place!",
  "createdAt": "2025-11-18T12:34:56.123456"
}
```

## Quick Start
### Prerequisites
- Java 21+ installed
- PostgreSQL running on localhost:5432 (see [POSTGRESQL_SETUP.md](docs/POSTGRESQL_SETUP.md) for setup)

### Run the application
```bash
./gradlew bootRun
```
(Default port: 8080)

Or run directly from IntelliJ IDEA by running `DemoApplication.java`

### Test with curl
```bash
# Create an entry
curl -X POST http://localhost:8080/guestbook \
  -H "Content-Type: application/json" \
  -d '{"name":"Bob","message":"I liked it here"}'

# List entries
curl http://localhost:8080/guestbook
```

## Architecture Overview
For detailed architectural rationale and layering rules see [ARCHITECTURE.md](docs/ARCHITECTURE.md).

- **Controller**: `GuestbookController` exposes REST endpoints.
- **Service**: `GuestbookService` coordinates creation and retrieval.
- **Repository Abstraction**: `GuestbookRepository` defines persistence contract.
- **Infrastructure**: 
  - `PostgresGuestbookRepository` (active, marked `@Primary`) - adapts domain layer to JPA
  - `JpaGuestbookRepository` - Spring Data JPA interface for database operations
  - `GuestbookEntryEntity` - JPA entity with database annotations
  - `GuestbookEntryMapper` - maps between domain model and JPA entity
  - `InMemoryGuestbookRepository` (available as fallback)
  - `DatabaseInitializer` - ensures `guestbook_db` exists before application starts

## Extensibility
To switch repository implementations, either:
- Change the `@Primary` annotation to a different implementation
- Use Spring profiles to activate different repositories
- Remove `@Primary` and use `@Qualifier` for explicit selection

The service layer (`GuestbookService`) depends only on the `GuestbookRepository` interface, making implementations fully swappable without changing business logic.
