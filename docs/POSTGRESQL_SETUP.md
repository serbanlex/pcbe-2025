# PostgreSQL Repository Setup Guide

## Overview
This guide documents the PostgreSQL repository implementation added to the Guestbook application.

## What Was Added

### 1. Dependencies (build.gradle)
- `spring-boot-starter-data-jpa` - JPA/Hibernate support
- `postgresql` - PostgreSQL JDBC driver

### 2. Database Configuration (application.properties)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/guestbook_db
spring.datasource.username=alex
spring.datasource.password=postgres
```
**NOTE**: In production, use environment variables for credentials!

### 3. New Components

#### Infrastructure Layer - Persistence Package
- **GuestbookEntryEntity** (`infrastructure/persistence/`)
  - JPA entity with `@Entity` and `@Table` annotations
  - Maps to `guestbook_entries` table
  - Fields: id (String), name, message, createdAt

- **JpaGuestbookRepository** (`infrastructure/persistence/`)
  - Spring Data JPA repository interface
  - Extends `JpaRepository<GuestbookEntryEntity, String>`
  - Provides CRUD operations automatically

#### Infrastructure Layer - Mappers Package
- **GuestbookEntryMapper** (`infrastructure/mappers/`)
  - Converts between domain model (`GuestbookEntry`) and JPA entity (`GuestbookEntryEntity`)
  - Methods: `toEntity()`, `toDomain()`
  - Keeps JPA annotations isolated from domain layer

#### Infrastructure Layer - Repositories Package
- **PostgresGuestbookRepository** (`infrastructure/repositories/`)
  - Implements `GuestbookRepository` interface
  - Marked with `@Primary` to override `InMemoryGuestbookRepository`
  - Uses constructor injection for `JpaGuestbookRepository` and `GuestbookEntryMapper`
  - Delegates to JPA repository and maps results to domain objects

#### Infrastructure Layer - Config Package
- **DatabaseInitializer** (`infrastructure/config/`)
  - Creates database if it doesn't exist on startup
  - Uses `@PostConstruct` to run after bean creation
  - Connects to postgres database to check/create guestbook_db

## Architecture Compliance

- **Domain Layer** - No changes, remains framework-agnostic
- **Application Layer** - No changes, still depends only on `GuestbookRepository` interface
- **Infrastructure Layer** - All PostgreSQL-specific code isolated here
- **API Layer** - No changes needed

## How It Works

1. **Application Startup**:
   - Spring initializes beans
   - `DatabaseInitializer` creates database if needed
   - JPA creates/updates table schema (`ddl-auto=update`)

2. **Dependency Injection**:
   - `PostgresGuestbookRepository` marked as `@Primary`
   - Spring injects this instead of `InMemoryGuestbookRepository` into `GuestbookService`
   - Service code remains unchanged - it still calls the same interface methods

3. **Data Flow**:
   ```
   Controller → Service → GuestbookRepository (interface)
                             ↓
                       PostgresGuestbookRepository (@Primary)
                             ↓
                       GuestbookEntryMapper
                             ↓
                       JpaGuestbookRepository → Database
   ```

## Prerequisites

### 1. PostgreSQL Installation
```bash
# macOS (Homebrew)
brew install postgresql@15
brew services start postgresql@15

# Or use Docker
docker run --name postgres-dev \
  -e POSTGRES_USER=alex \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15
```

### 2. Create User (if needed)
```bash
psql postgres
CREATE USER alex WITH PASSWORD 'postgres';
ALTER USER alex CREATEDB;
\q
```

## Running the Application

### Option 1: Gradle (if build works)
```bash
./gradlew bootRun
```

### Option 2: IntelliJ IDEA
1. Open the project
2. Wait for Gradle sync
3. Right-click on `DemoApplication.java`
4. Select "Run 'DemoApplication'"

### Option 3: Direct Java (after building)
```bash
java -jar build/libs/demo-0.0.1-SNAPSHOT.jar
```

## Testing

### 1. Check Database
```bash
psql -U alex -d guestbook_db
\dt  # List tables
SELECT * FROM guestbook_entries;
```

### 2. Test API
```bash
# Create entry
curl -X POST http://localhost:8080/guestbook \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","message":"Testing PostgreSQL!"}'

# List entries
curl http://localhost:8080/guestbook
```

## Switching Between Implementations

### Use PostgreSQL (Current - Default)
The `@Primary` annotation on `PostgresGuestbookRepository` makes it the default.

### Use In-Memory (For Testing)
**Option 1**: Remove `@Primary` from `PostgresGuestbookRepository`
```java
@Repository
// @Primary  // Comment this out
public class PostgresGuestbookRepository implements GuestbookRepository {
```

**Option 2**: Use Spring Profiles
```java
// PostgresGuestbookRepository
@Repository
@Primary
@Profile("prod")
public class PostgresGuestbookRepository implements GuestbookRepository {

// InMemoryGuestbookRepository
@Repository
@Profile("dev")
public class InMemoryGuestbookRepository implements GuestbookRepository {
```

Then run with: `./gradlew bootRun --args='--spring.profiles.active=dev'`

## Database Schema

The JPA entity will create this table:
```sql
CREATE TABLE guestbook_entries (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```