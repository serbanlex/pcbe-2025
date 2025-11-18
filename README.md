# Demo Guestbook Service

## Purpose
A simple Spring Boot application demonstrating a layered architecture (domain, application/service, infrastructure, controllers) for a Guestbook feature. Users can sign the guestbook with their name and a message. The service layer depends only on the `GuestbookRepository` interface, enabling easy swapping of persistence implementations.

## Current State
- Active repository implementation: In-memory (`InMemoryGuestbookRepository`) using a ConcurrentHashMap.
- A PostgreSQL-backed implementation is being prepared but not yet active.
- Domain model: `GuestbookEntry` (id, name, message, createdAt).

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
### Run the application
```bash
./gradlew bootRun
```
(Default port: 8081)

### Test with curl
```bash
# Create an entry
curl -X POST http://localhost:8081/guestbook \
  -H "Content-Type: application/json" \
  -d '{"name":"Bob","message":"I liked it here"}'

# List entries
curl http://localhost:8081/guestbook
```

## Architecture Overview
For detailed architectural rationale and layering rules see [ARCHITECTURE.md](ARCHITECTURE.md).

- Controller: `GuestbookController` exposes REST endpoints.
- Service: `GuestbookService` coordinates creation and retrieval.
- Repository Abstraction: `GuestbookRepository` defines persistence contract.
- Infrastructure: `InMemoryGuestbookRepository` (current), future `PostgresGuestbookRepository`.

## Extensibility
To switch to a database-backed repository later, implement `GuestbookRepository` and mark the preferred bean with `@Primary` or use profiles/qualifiers.

## Next Steps (Planned)
- Enable PostgreSQL repository implementation.
