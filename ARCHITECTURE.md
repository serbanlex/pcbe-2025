# Architecture: Guestbook Service

## Purpose
This document describes the architectural layout, layering rules, and extension points of the Guestbook service. The application demonstrates clean separation between domain logic, application orchestration, infrastructure concerns, and delivery (HTTP API).

## High-Level Overview
```
Controller (HTTP) → Service (Application) → Repository (Interface) ← Implementation (Infra)
                                        ↑
                                      Domain Model
```
Only the application layer depends on the repository abstraction. Infrastructure supplies implementations. Controllers depend on the service only. Domain contains pure data structures (no framework dependencies).

## Layer Breakdown
| Layer | Package Roots | Responsibilities | Depends On |
|-------|---------------|------------------|------------|
| Domain | `alex.pcbe.demo.domain.entities` | Core data structures (`GuestbookEntry`) | (None – pure Java) |
| Application | `alex.pcbe.demo.application.services`, `alex.pcbe.demo.application.repositories` | Use cases / business operations (`GuestbookService`), repository abstraction (`GuestbookRepository`) | Domain |
| Infrastructure | `alex.pcbe.demo.infrastructure.repositories`, `alex.pcbe.demo.infrastructure.persistence`, `alex.pcbe.demo.infrastructure.mappers`, `alex.pcbe.demo.infrastructure.config` | Technical implementations (In-memory + planned Postgres), mapping, startup tasks | Application (interfaces), Domain (models for mapping) |
| Delivery (API) | `alex.pcbe.demo.controllers`, `alex.pcbe.demo.controllers.dto` | HTTP endpoints, request/response mapping | Application, Domain |

## Current Implementations
- Active: `InMemoryGuestbookRepository` (ConcurrentHashMap)
- Planned: `PostgresGuestbookRepository` (JPA / Hibernate + `GuestbookEntryEntity`)
- Mapper: `GuestbookEntryMapper` converts between JPA entity and domain model.

## Domain Model
`GuestbookEntry` fields:
- `id` (String UUID)
- `name` (String)
- `message` (String)
- `createdAt` (LocalDateTime)

Domain objects are created inside the service (factory-style via constructor) ensuring timestamps and IDs are assigned consistently.

## Application Logic (Service)
`GuestbookService` exposes two operations:
1. `createEntry(name, message)` – Validates (future), constructs domain object, persists via repository.
2. `getAllEntries()` – Retrieves all domain objects from repository.

## Repository Abstraction
`GuestbookRepository` defines:
- `save(GuestbookEntry)`
- `findAll()`
- `findById(String)` (extensible even if not yet exposed at API level)

## Infrastructure Strategies
### InMemoryGuestbookRepository
- Simple, thread-safe storage via `ConcurrentHashMap`.
- Useful for local dev and tests.

### PostgresGuestbookRepository (Planned/Scaffolded)
- Relies on Spring Data JPA (`JpaGuestbookRepository`) + `GuestbookEntryEntity`.
- Mapping layer (`GuestbookEntryMapper`) isolates JPA annotations from domain.
- Marked `@Primary` when active to override in-memory implementation.

### Database Initialization
`DatabaseInitializer` ensures the configured PostgreSQL database exists (creating it if missing) using a connection to the default `postgres` catalog.

## Dependency Rules (Enforced by Convention)
- Controllers → Service (never directly to infrastructure).
- Service → Repository interface only (never to concrete class).
- Infrastructure → Implements application interfaces; allowed to reference domain for mapping.
- Domain → No outward dependencies.

## Data Flow Examples
### Create Entry
```
HTTP POST /guestbook
  → GuestbookController.parse(CreateGuestbookEntryRequest)
    → GuestbookService.createEntry(name, message)
      → new GuestbookEntry(name, message, generated id, timestamp)
      → guestbookRepository.save(domain)
        → (InMemory or Postgres implementation)
      ← persisted domain object
  ← JSON response
```

### Get All Entries
```
HTTP GET /guestbook
  → GuestbookController.getAllEntries()
    → GuestbookService.getAllEntries()
      → guestbookRepository.findAll()
        → returns domain list
    ← list
  ← JSON array response
```

## Switching Persistence Implementation
1. Keep `GuestbookRepository` unchanged.
2. Provide new implementation (e.g., Postgres) annotated with `@Repository`.
3. Mark preferred bean with `@Primary` OR use profiles:
   - `@Profile("prod")` for Postgres
   - `@Profile("dev")` for InMemory
4. Start app with `spring.profiles.active=prod`.

## Extension Points
- Add validation (e.g., reject blank name/message) in `GuestbookService`.
- Add paging to `getAllEntries()`.
- Add `GET /guestbook/{id}` using `findById`.
- Add soft delete or edit functionality.
- Add integration tests targeting Postgres.

## Architectural Rationale
- Separation allows independent evolution (e.g., swap JPA for MongoDB without touching controllers/service logic).
- Domain remains framework-agnostic for portability and testability.
- Mapper pattern prevents leaking persistence annotations into domain.
- Constructor injection promotes immutability and clear dependencies.

## Future Improvements
- Explicit validation layer (Bean Validation) once JPA active.
- Observability (structured logging, metrics).
- Migration tool (Flyway/Liquibase) instead of `ddl-auto=update`.
- Replace direct DB creation with provisioning in infrastructure scripts.

## Reference Diagram
```
            +---------------------+
Request --> | GuestbookController | --> GuestbookService --> GuestbookRepository (interface)
            +---------------------+                               ^
                                                                  |
                           (InMemoryGuestbookRepository) OR (PostgresGuestbookRepository)
```

## How to Read This File
Use this as the source of truth for layering and allowed dependencies when contributing new features.

---
_Last updated: 2025-11-18_
