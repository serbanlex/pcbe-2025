# Spring Boot Learning Repository – PCBE 2025

A didactical project showcasing progressive implementations of a Spring Boot Guestbook application. Each branch demonstrates a different architectural pattern and technology integration, helping students understand how enterprise applications evolve from simple prototypes to production-ready systems.

---

## Repository Structure

This repository contains three educational branches, each building upon core Spring concepts with increasing complexity:

| Branch | Focus | Difficulty |
|--------|-------|------------|
| [dummy-implementation](#1-dummy-implementation-branch) | Spring basics, DI, in-memory storage | Beginner |
| [jpa-postgresql-implementation](#2-jpa-postgresql-implementation-branch) | JPA/Hibernate, PostgreSQL, Repository pattern | Intermediate |
| [rabbitmq-implementation](#3-rabbitmq-implementation-branch) | Message queues, event-driven architecture | Advanced |

---

## Core Architecture (All Branches)

All branches follow the same layered architecture pattern:

```
+-------------------------------------------------------------+
|                       API/Controller Layer                   |
|                 (HTTP endpoints, request/response DTOs)      |
+-------------------------------------------------------------+
|                      Application/Service Layer               |
|                  (Business logic, use case orchestration)    |
+-------------------------------------------------------------+
|                       Repository Interface                   |
|                 (Contract - no implementation details)       |
+-------------------------------------------------------------+
|                     Infrastructure Layer                     |
|         (Concrete implementations - varies by branch)        |
+-------------------------------------------------------------+
```

### Domain Model

```java
GuestbookEntry {
    String id;           // UUID
    String name;         // Author name
    String message;      // Entry content
    LocalDateTime createdAt;
}
```

### REST API Endpoints

```
GET  /guestbook      - Retrieve all entries
POST /guestbook      - Create new entry
```

---

## 1. dummy-implementation Branch

**Branch URL:** https://github.com/serbanlex/pcbe-2025/tree/dummy-implementation

### 1.1 Purpose

A minimal, zero-dependency implementation that demonstrates Spring Boot fundamentals without external services. Perfect for understanding how Spring wires components together.

### 1.2 What You Will Learn

1. **Spring Dependency Injection / IoC** - Constructor injection in `GuestbookService`
2. **@Repository annotation** - See `InMemoryGuestbookRepository`
3. **@Service annotation** - Business logic in `GuestbookService`
4. **@RestController** - HTTP endpoints in `GuestbookController`
5. **Interface-based design** - The `GuestbookRepository` abstraction
6. **Thread-safe storage** - `ConcurrentHashMap` usage for concurrent access

### 1.3 Key Files

```
src/main/java/alex/pcbe/demo/
    DemoApplication.java                           - Spring Boot entry point
    api/
        GuestbookController.java                   - REST endpoints
        dto/CreateGuestbookEntryRequest.java
    application/
        repositories/GuestbookRepository.java      - Interface (contract)
        services/GuestbookService.java             - Business logic
    domain/entities/
        GuestbookEntry.java                        - Domain model
    infrastructure/repositories/
        InMemoryGuestbookRepository.java           - ConcurrentHashMap storage
```

### 1.4 How to Run

```bash
git checkout dummy-implementation
./gradlew bootRun
```

Test with:
```bash
# Create an entry
curl -X POST http://localhost:8081/guestbook \
  -H "Content-Type: application/json" \
  -d '{"name":"Student","message":"Hello Spring!"}'

# List entries
curl http://localhost:8081/guestbook
```

### 1.5 Key Takeaway

Spring automatically injects `InMemoryGuestbookRepository` into `GuestbookService` because it is the only `@Repository` bean implementing `GuestbookRepository`. No `new` keyword needed.

---

## 2. jpa-postgresql-implementation Branch

**Branch URL:** https://github.com/serbanlex/pcbe-2025/tree/jpa-postgresql-implementation

### 2.1 Purpose

Demonstrates real database persistence using JPA/Hibernate with PostgreSQL. Shows how to swap implementations without changing business logic - the power of dependency injection.

### 2.2 What You Will Learn

1. **JPA Entity mapping** - `GuestbookEntryEntity` with `@Entity`, `@Table`, `@Column`
2. **Spring Data JPA** - `JpaGuestbookRepository extends JpaRepository`
3. **@Primary annotation** - `PostgresGuestbookRepository` overrides in-memory
4. **Mapper pattern** - `GuestbookEntryMapper` converts entity to domain and back
5. **Database initialization** - `DatabaseInitializer` creates database if missing
6. **Hibernate auto-DDL** - `spring.jpa.hibernate.ddl-auto=update`

### 2.3 Key Files

```
src/main/java/alex/pcbe/demo/
    infrastructure/
        config/
            DatabaseInitializer.java               - Creates DB on startup
        db/
            GuestbookEntryEntity.java              - JPA entity (with annotations)
            JpaGuestbookRepository.java            - Spring Data interface
        mappers/
            GuestbookEntryMapper.java              - Entity <-> Domain conversion
        repositories/
            InMemoryGuestbookRepository.java
            PostgresGuestbookRepository.java       - @Primary implementation
```

### 2.4 Configuration (application.properties)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/guestbook_db
spring.datasource.username=alex
spring.datasource.password=pcbe
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### 2.5 Prerequisites and How to Run

1. Install PostgreSQL and create a user
2. The application creates `guestbook_db` automatically on startup
3. Run:

```bash
git checkout jpa-postgresql-implementation
./gradlew bootRun
```

### 2.6 Key Takeaway

By using `@Primary` on `PostgresGuestbookRepository`, Spring now injects the PostgreSQL implementation instead of the in-memory one. The `GuestbookService` code did not change at all:

```java
@Repository
@Primary  // This makes PostgresGuestbookRepository the default
public class PostgresGuestbookRepository implements GuestbookRepository {
    // ...
}
```

---

## 3. rabbitmq-implementation Branch

**Branch URL:** https://github.com/serbanlex/pcbe-2025/tree/rabbitmq-implementation

### 3.1 Purpose

Introduces asynchronous, event-driven architecture using RabbitMQ. When a guestbook entry is created, an event is published to a message queue, demonstrating loosely-coupled microservice communication patterns.

### 3.2 What You Will Learn

1. **RabbitMQ configuration** - `RabbitMQConfig` defines exchange, queue, and binding
2. **Event publishing** - `GuestbookEventPublisher` uses `RabbitTemplate`
3. **Event consumption** - `GuestbookEventListener` uses `@RabbitListener`
4. **Event/Message DTOs** - `GuestbookEntryAddedEvent` as a Java record
5. **JSON serialization** - `Jackson2JsonMessageConverter` for message format
6. **Configuration properties** - `RabbitProperties` with `@ConfigurationProperties`

### 3.3 Key Files

```
src/main/java/alex/pcbe/demo/
    application/services/
        GuestbookService.java                      - Publishes event after save
        GuestbookEventPublisher.java               - Sends to RabbitMQ
        GuestbookEventListener.java                - Receives from RabbitMQ
    domain/events/
        GuestbookEntryAddedEvent.java              - Event payload (record)
    infrastructure/config/
        RabbitMQConfig.java                        - Exchange + Queue + Binding
        RabbitProperties.java                      - Config mapping
```

### 3.4 Message Flow

```
POST /guestbook
       |
       v
  GuestbookService.createEntry()
       |
       +---> Repository.save()
       |
       +---> EventPublisher.publishEntryAdded() ---> RabbitMQ Exchange
                                                          |
                                                          v
                                                GuestbookEventListener
                                                     (logs event)
```

### 3.5 Configuration (application.properties)

```properties
spring.rabbitmq.host=${SPRING_RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${SPRING_RABBITMQ_PORT:5672}
spring.rabbitmq.username=${SPRING_RABBITMQ_USERNAME:guest}
spring.rabbitmq.password=${SPRING_RABBITMQ_PASSWORD:guest}

# Custom properties
app.rabbitmq.exchange=guestbook.exchange
app.rabbitmq.queue=guestbook.queue
app.rabbitmq.routing-key=guestbook.entry.added
```

### 3.6 Prerequisites and How to Run

1. Install RabbitMQ or use Docker:
   ```bash
   docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:management
   ```
2. Access RabbitMQ Management UI at http://localhost:15672 (credentials: guest/guest)
3. Run the application:
   ```bash
   git checkout rabbitmq-implementation
   ./gradlew bootRun
   ```
4. Watch the logs to see events being published and consumed

### 3.7 Key Takeaway

The `GuestbookService` does not need to know who consumes the event or how it is processed. It just publishes to the exchange. This enables scalable, decoupled architectures where multiple services can react to the same event.

---

## Learning Path Recommendations

### For Beginners

1. Start with `dummy-implementation` to understand DI and Spring annotations
2. Practice adding a new endpoint (e.g., `GET /guestbook/{id}`)
3. Try creating a new repository implementation (e.g., file-based)

### For Intermediate Students

1. Compare `dummy-implementation` with `jpa-postgresql-implementation`
2. Notice what did not change (Service, Controller, Domain)
3. Study the mapper pattern and why it is useful
4. Add validation (e.g., reject blank messages)

### For Advanced Students

1. Study `rabbitmq-implementation` to understand pub/sub patterns
2. Try adding a second listener (e.g., send email notifications)
3. Explore error handling and dead letter queues
4. Consider: how would you combine JPA + RabbitMQ?

---

## Common Setup

All branches use:
- Java 25 (or configure your version)
- Spring Boot 3.5.7
- Gradle build system
- Lombok for boilerplate reduction

```bash
# Clone the repository
git clone https://github.com/serbanlex/pcbe-2025.git
cd pcbe-2025

# Switch to desired branch
git checkout <branch-name>

# Run
./gradlew bootRun
```

---

## Keywords and Concepts Index

| Term | Branch(es) | Description |
|------|------------|-------------|
| @Autowired / Constructor Injection | All | Dependency injection |
| @Repository | All | Data access layer marker |
| @Service | All | Business logic layer marker |
| @RestController | All | HTTP endpoint handler |
| @Primary | JPA | Default bean selection |
| @Entity, @Table | JPA | JPA/Hibernate mapping |
| JpaRepository | JPA | Spring Data magic interfaces |
| @RabbitListener | RabbitMQ | Message consumer |
| RabbitTemplate | RabbitMQ | Message producer |
| Exchange, Queue, Binding | RabbitMQ | RabbitMQ topology |

---

## Questions?

This is a learning repository for PCBE (Programare Concurentă și Bazată pe Evenimente) course at Universitatea Politehnica Timisoara.

Feel free to explore, experiment, and break things - that is how we learn.
