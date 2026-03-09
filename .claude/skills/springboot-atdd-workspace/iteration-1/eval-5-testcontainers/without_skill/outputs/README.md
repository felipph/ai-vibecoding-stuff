# Testing UserRepository with Testcontainers and PostgreSQL

This guide demonstrates how to test complex JPQL queries using Testcontainers with a real PostgreSQL database.

## Overview

The solution provides:
- Real PostgreSQL database for integration testing
- Complex JPQL query testing with multiple criteria
- Pagination and sorting tests
- Native query support
- Comprehensive test coverage

## Project Structure

```
src/
├── main/
│   └── java/
│       └── com/example/
│           ├── user/
│           │   ├── domain/
│           │   │   ├── User.java
│           │   │   └── UserStatus.java
│           │   └── repository/
│           │       └── UserRepository.java
│           └── config/
│               └── TestcontainersConfiguration.java
└── test/
    └── java/
        └── com/example/
            └── user/
                └── repository/
                    └── UserRepositoryIntegrationTest.java
```

## Key Components

### 1. User Entity (`User.java`)
JPA entity with:
- Basic fields: name, email, status, registrationDate
- Indexes on frequently queried columns
- Factory methods for creation
- Audit fields (createdAt, updatedAt)

### 2. UserRepository (`UserRepository.java`)
Repository with complex JPQL queries:
- `findByMultipleCriteria()` - Multiple optional criteria
- `findByMultipleCriteriaPaginated()` - Pagination support
- `searchByNameOrEmail()` - Flexible search
- `countByCriteria()` - Count with criteria
- Native query support

### 3. TestcontainersConfiguration (`TestcontainersConfiguration.java`)
Configuration class that:
- Uses PostgreSQL 16 Alpine container
- Implements @ServiceConnection pattern
- Enables container reuse for performance

### 4. UserRepositoryIntegrationTest (`UserRepositoryIntegrationTest.java`)
Comprehensive test suite with:
- Nested test classes for organization
- Tests for all query variations
- Edge case coverage
- Performance tests

## Setup Instructions

### 1. Add Dependencies to pom.xml

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers-bom</artifactId>
            <version>2.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Testcontainers -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-testcontainers</artifactId>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>testcontainers-junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>testcontainers-postgresql</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 2. Ensure Docker is Running

Testcontainers requires Docker to be running on your machine:
```bash
# Verify Docker is running
docker ps

# If not running, start Docker Desktop or Docker daemon
```

### 3. Run Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=UserRepositoryIntegrationTest

# Run specific test method
./mvnw test -Dtest=UserRepositoryIntegrationTest#shouldFilterByAllCriteria
```

## Key Testing Patterns

### Pattern 1: @DataJpaTest with Testcontainers

```java
@DataJpaTest
@Import(TestcontainersConfiguration.class)
@Testcontainers
class UserRepositoryIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    // Tests here
}
```

Benefits:
- Loads only JPA components (fast)
- Uses real PostgreSQL database
- Automatic schema creation

### Pattern 2: Test Configuration

```java
@TestConfiguration(proxyBeanMethods = false)
@Testcontainers
public class TestcontainersConfiguration {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withReuse(true);

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return postgres;
    }
}
```

Benefits:
- Reusable across multiple test classes
- @ServiceConnection auto-configures DataSource
- Static container shared across tests

### Pattern 3: Comprehensive Test Coverage

```java
@Nested
@DisplayName("Complex JPQL Query - Multiple Criteria")
class MultipleCriteriaQuery {

    @Test
    @DisplayName("Should filter by name only")
    void shouldFilterByNameOnly() {
        List<User> results = userRepository.findByMultipleCriteria(
            "Silva", null, null, null, null
        );
        assertThat(results).hasSize(1);
    }

    // More tests...
}
```

Benefits:
- Organized by functionality
- Clear test names
- Comprehensive coverage

## Testing Best Practices

### 1. Use Realistic Test Data

```java
@BeforeEach
void setUp() {
    activeUser1 = User.create(
        "João Silva",
        "joao.silva@example.com",
        UserStatus.ACTIVE,
        LocalDate.of(2024, 1, 15)
    );
    // More test users...
}
```

### 2. Test All Query Variations

- Individual criteria (name, email, status, dates)
- Combined criteria
- Null criteria (should return all)
- Edge cases (empty results, special characters)
- Case sensitivity
- Partial matches

### 3. Test Pagination and Sorting

```java
@Test
void shouldPaginateResults() {
    PageRequest pageRequest = PageRequest.of(0, 2);
    Page<User> page = repository.findByMultipleCriteriaPaginated(
        null, null, null, null, null, pageRequest
    );

    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getTotalElements()).isEqualTo(4);
}
```

### 4. Clean Up Between Tests

```java
@BeforeEach
void setUp() {
    userRepository.deleteAll();
    // Create fresh test data
}
```

## Performance Optimization

### Container Reuse

```java
@Container
static PostgreSQLContainer<?> postgres =
    new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
        .withReuse(true);  // Reuse container across test runs
```

Add to `~/.testcontainers.properties`:
```properties
testcontainers.reuse.enable=true
```

### Shared Configuration

Use a single `TestcontainersConfiguration` class across all repository tests:

```java
@DataJpaTest
@Import(TestcontainersConfiguration.class)  // Shared config
class OrderRepositoryIntegrationTest {
    // Tests
}
```

## Common Issues and Solutions

### Issue 1: Docker Not Running

**Error:** `Could not find a valid Docker environment`

**Solution:** Start Docker Desktop or Docker daemon

### Issue 2: Port Conflicts

**Error:** `Port 5432 already in use`

**Solution:** Testcontainers uses random ports automatically. Ensure no local PostgreSQL is running.

### Issue 3: Slow Test Execution

**Problem:** Tests take too long

**Solutions:**
- Enable container reuse: `.withReuse(true)`
- Use `@DataJpaTest` instead of `@SpringBootTest`
- Share containers across test classes

### Issue 4: Schema Not Created

**Problem:** Tables don't exist

**Solution:** Ensure `spring.jpa.hibernate.ddl-auto=create-drop` in test properties

## Advanced Features

### 1. Custom Initialization Scripts

```java
@Container
static PostgreSQLContainer<?> postgres =
    new PostgreSQLContainer<>("postgres:16-alpine")
        .withInitScript("db/init.sql");  // src/test/resources/db/init.sql
```

### 2. Database-Specific Features

Test PostgreSQL-specific features:
- JSONB columns
- Array types
- Full-text search
- Custom functions

### 3. Multiple Containers

```java
@TestConfiguration
class MultiContainerConfig {
    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>("postgres:16-alpine");
    }

    @Bean
    @ServiceConnection
    GenericContainer<?> redis() {
        return new GenericContainer<>("redis:7-alpine");
    }
}
```

## Summary

This setup provides:
- **Real Database Testing**: Actual PostgreSQL behavior
- **Complex Query Validation**: Test JPQL with multiple criteria
- **Fast Execution**: Container reuse and JPA slice testing
- **Comprehensive Coverage**: All query variations tested
- **Best Practices**: Following Spring Boot 4 / Testcontainers 2.x patterns

The test suite ensures your JPQL queries work correctly with a real database, catching issues that H2 or other in-memory databases might miss.
