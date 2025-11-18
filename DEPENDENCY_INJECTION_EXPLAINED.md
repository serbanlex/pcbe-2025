# Dependency Injection Analysis: Who Decides Which Repository?

## Answer: **Spring's IoC Container** (via Auto-Configuration)

The decision of which `GuestbookRepository` implementation gets injected into `GuestbookService` is made by **Spring Framework's Inversion of Control (IoC) Container**, specifically through its **autowiring mechanism**.

## How It Works

### 1. **Component Scanning** (`@SpringBootApplication`)

```java
@SpringBootApplication  // In DemoApplication.java
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

The `@SpringBootApplication` annotation includes:
- `@ComponentScan` - Scans the package `alex.pcbe.demo` and all sub-packages
- `@EnableAutoConfiguration` - Enables Spring Boot's auto-configuration
- `@Configuration` - Marks this as a configuration class

### 2. **Bean Registration**

During application startup, Spring scans and registers these as beans:

**Service Bean:**
```java
@Service  // <- Registers as a Spring bean
public class GuestbookService {
    private final GuestbookRepository guestbookRepository;
    
    public GuestbookService(GuestbookRepository guestbookRepository) { // <- Needs injection
        this.guestbookRepository = guestbookRepository;
    }
}
```

**Repository Bean:**
```java
@Repository  // <- Registers as a Spring bean
public class InMemoryGuestbookRepository implements GuestbookRepository {
    // Implementation...
}
```

### 3. **Autowiring by Type**

When Spring creates the `GuestbookService` bean, it:
1. Sees the constructor requires a `GuestbookRepository` parameter
2. Searches the IoC container for beans of type `GuestbookRepository`
3. Finds `InMemoryGuestbookRepository` (it implements `GuestbookRepository`)
4. **Automatically injects** it into the constructor

This is called **Constructor Injection** (recommended approach).

## The Decision Algorithm

Spring uses this logic to decide which implementation to inject:

```
1. Look for beans of type GuestbookRepository (interface)
2. Find implementations:
   ✓ InMemoryGuestbookRepository (marked with @Repository)
3. If ONE implementation found → Inject it
4. If MULTIPLE implementations found → Error (unless @Primary or @Qualifier used)
5. If ZERO implementations found → Error (NoSuchBeanDefinitionException)
```

## Current State: Why InMemoryGuestbookRepository?

**Currently, Spring injects `InMemoryGuestbookRepository` because:**
- It's the **only** class that implements `GuestbookRepository`
- It's annotated with `@Repository` (making it a Spring bean)
- It's in a package scanned by Spring (`alex.pcbe.demo.infrastructure.repositories`)

## What If You Add Another Implementation?

Let's say you create a second implementation:

```java
@Repository
public class DatabaseGuestbookRepository implements GuestbookRepository {
    // JPA implementation
}
```

**Problem:** Spring will find TWO beans of type `GuestbookRepository` and throw an error:
```
***************************
APPLICATION FAILED TO START
***************************

Description:
Parameter 0 of constructor in alex.pcbe.demo.application.services.GuestbookService required a single bean, but 2 were found
```

### Solution 1: Use `@Primary`

```java
@Repository
@Primary  // <- This one will be chosen by default
public class InMemoryGuestbookRepository implements GuestbookRepository {
    // ...
}
```

### Solution 2: Use `@Qualifier`

```java
@Service
public class GuestbookService {
    private final GuestbookRepository guestbookRepository;
    
    public GuestbookService(@Qualifier("inMemoryGuestbookRepository") GuestbookRepository guestbookRepository) {
        this.guestbookRepository = guestbookRepository;
    }
}
```

### Solution 3: Use `@Profile`

```java
@Repository
@Profile("dev")  // Only active when profile is "dev"
public class InMemoryGuestbookRepository implements GuestbookRepository {
    // ...
}

@Repository
@Profile("prod")  // Only active when profile is "prod"
public class DatabaseGuestbookRepository implements GuestbookRepository {
    // ...
}
```

Activate via `application.properties`:
```properties
spring.profiles.active=dev
```

### Solution 4: Manual Configuration (Explicit)

Create a configuration class:

```java
@Configuration
public class RepositoryConfig {
    
    @Bean
    public GuestbookRepository guestbookRepository() {
        // Explicitly decide which implementation to return
        return new InMemoryGuestbookRepository();
        // or
        // return new DatabaseGuestbookRepository();
    }
}
```

## Key Benefits of This Approach

1. **Loose Coupling**: `GuestbookService` doesn't know or care about the concrete implementation
2. **Easy Testing**: You can inject mock implementations in unit tests
3. **Flexibility**: Switch implementations without changing service code
4. **Follows Dependency Inversion Principle**: High-level module (service) depends on abstraction (interface), not concrete class

## Architecture Diagram

```
┌─────────────────────────────────────────────────┐
│         Spring IoC Container                     │
│  ┌───────────────────────────────────────────┐  │
│  │  Beans Registry                           │  │
│  │  ────────────────────────────────────────  │  │
│  │  • GuestbookService (@Service)            │  │
│  │  • InMemoryGuestbookRepository            │  │
│  │    (@Repository)                          │  │
│  │  • GuestbookController (@RestController)  │  │
│  └───────────────────────────────────────────┘  │
│                                                   │
│  Autowiring Process:                             │
│  1. Create InMemoryGuestbookRepository bean      │
│  2. Create GuestbookService bean                 │
│     → Inject InMemoryGuestbookRepository         │
│  3. Create GuestbookController bean              │
│     → Inject GuestbookService                    │
└─────────────────────────────────────────────────┘

Application Layer (alex.pcbe.demo.application)
├── GuestbookService ──depends on──→ GuestbookRepository (interface)
                                               ↑
                                               │ implements
Infrastructure Layer (alex.pcbe.demo.infrastructure)
├── InMemoryGuestbookRepository ───────────────┘
```

## Summary

**Who decides?** → **Spring's IoC Container** via:
- Component scanning (`@SpringBootApplication`)
- Bean registration (`@Repository`, `@Service`, `@RestController`)
- Autowiring by type (constructor injection)

**Current decision:** `InMemoryGuestbookRepository` is injected because it's the only implementation of `GuestbookRepository` registered as a Spring bean.

**To change it:** Add another implementation with `@Repository` and use `@Primary`, `@Qualifier`, or `@Profile` to control which one gets injected.

