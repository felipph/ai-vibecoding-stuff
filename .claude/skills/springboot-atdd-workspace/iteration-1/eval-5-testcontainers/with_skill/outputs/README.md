# Testing Complex JPQL Queries with Testcontainers and PostgreSQL

This guide demonstrates how to test complex JPQL queries in a Spring Boot UserRepository using Testcontainers with a real PostgreSQL database.

## Why Testcontainers?

When testing complex JPQL queries, using a real database (PostgreSQL) instead of H2 provides several benefits:

1. **Native Query Compatibility**: PostgreSQL-specific functions (ILIKE, date operations) work correctly
2. **Real Database Behavior**: Catches issues that only appear in production
3. **Schema Validation**: Ensures your entities map correctly to actual database tables
4. **Constraint Testing**: Verifies unique constraints, foreign keys, and check constraints

## Project Structure

```
src/main/java/com/example/
  ├── User.java                    # JPA Entity
  ├── UserStatus.java              # Enum for user status
  ├── UserSearchCriteria.java      # Search criteria DTO
  ├── UserRepository.java          # JPA Repository with complex query
  └── UserRepositoryApplication.java

src/main/resources/
  └── application.yml              # Application configuration

src/test/java/com/example/
  └── UserRepositoryTest.java      # Integration tests with Testcontainers

src/test/resources/
  └── application-test.yml         # Test-specific configuration
```

## Key Components

### 1. Testcontainers Configuration

The test class uses these annotations and configurations:

```java
@DataJpaTest
@Testcontainers
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

**Key points:**
- `@Container static` ensures one container per test class (better performance)
- `@DynamicPropertySource` configures Spring Boot to use the container's database
- PostgreSQL 16 Alpine is lightweight and fast to start

### 2. Complex JPQL Query

The repository includes a search query with multiple optional filters:

```java
@Query("""
    SELECT u FROM User u
    WHERE (:name IS NULL
           OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))
           OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
           OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%')))
      AND (:email IS NULL
           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
      AND (:status IS NULL
           OR u.status = :status)
      AND (:registrationDateFrom IS NULL
           OR u.registrationDate >= :registrationDateFrom)
      AND (:registrationDateTo IS NULL
           OR u.registrationDate <= :registrationDateTo)
    ORDER BY u.registrationDate DESC, u.lastName ASC
    """)
List<User> searchUsers(...);
```

### 3. ATDD Test Organization

Tests are organized by acceptance criteria using nested classes:

```java
@Nested
@DisplayName("searchUsers: by name")
class SearchByName {
    @Test
    void searchUsers_byPartialFirstName_returnsMatchingUsers() { ... }
    // More tests...
}
```

## Running the Tests

### Prerequisites

- Docker (required for Testcontainers)
- Java 17+
- Maven or Gradle

### Using Maven

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=UserRepositoryTest

# Run with verbose output
./mvnw test -Dtest=UserRepositoryTest -X
```

### Using Gradle

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests UserRepositoryTest

# Run with detailed output
./gradlew test --tests UserRepositoryTest --info
```

## Test Patterns Demonstrated

### 1. Arrange-Act-Assert Pattern

Each test follows the AAA pattern for clarity:

```java
@Test
void findByEmail_withExistingEmail_returnsUser() {
    // Arrange - set up test data (done in @BeforeEach)

    // Act - call the method being tested
    Optional<User> found = userRepository.findByEmail("john.doe@example.com");

    // Assert - verify expected outcome
    assertThat(found).isPresent();
    assertThat(found.get().getEmail()).isEqualTo("john.doe@example.com");
}
```

### 2. AssertJ Fluent Assertions

Using AssertJ for readable assertions:

```java
assertThat(results)
    .hasSize(2)
    .extracting(User::getEmail)
    .containsExactlyInAnyOrder("john.doe@example.com", "alice.doe@example.com");
```

### 3. Test Isolation

Each test is independent:
- `@BeforeEach` clears the database
- Fresh test data is created for each test
- No shared mutable state between tests

### 4. Nested Test Classes

Tests are grouped by functionality:

```java
@Nested
@DisplayName("searchUsers: by name")
class SearchByName { ... }

@Nested
@DisplayName("searchUsers: by date range")
class SearchByDateRange { ... }
```

## Acceptance Criteria Covered

The tests verify these business requirements:

| Criterion | Test Coverage |
|-----------|---------------|
| [+] User can be found by exact email | `findByEmail_withExistingEmail_returnsUser` |
| [-] Non-existent email returns empty | `findByEmail_withNonExistentEmail_returnsEmpty` |
| [+] Search by partial name (first, last, or full) | `SearchByName` nested class |
| [+] Search by partial email | `SearchByEmail` nested class |
| [+] Filter by status | `SearchByStatus` nested class |
| [+] Filter by date range | `SearchByDateRange` nested class |
| [+] Results ordered correctly | `searchUsers_returnsOrderedResults` |
| [-] Empty criteria returns all | `searchUsers_withAllNullCriteria_returnsAllUsers` |
| [+] Combined criteria | `SearchWithCombinedCriteria` nested class |

## Performance Considerations

### Container Reuse

For faster test execution, enable Testcontainers reuse:

```properties
# ~/.testcontainers.properties
testcontainers.reuse.enable=true
```

### Parallel Test Execution

The Maven Surefire plugin is configured for parallel execution:

```xml
<configuration>
    <parallel>methods</parallel>
    <threadCount>4</threadCount>
</configuration>
```

## Common Issues and Solutions

### 1. Docker Not Running

**Error**: `Could not find a valid Docker environment`

**Solution**: Ensure Docker Desktop or Docker Engine is running.

### 2. Port Conflicts

**Error**: `Port 5432 already in use`

**Solution**: Testcontainers uses random ports, but if this occurs, stop local PostgreSQL.

### 3. Slow Test Execution

**Cause**: Container startup time

**Solution**: Enable container reuse (see above) or use `@Container static` for class-level sharing.

## Extending the Tests

### Adding New Search Criteria

1. Add the field to `UserSearchCriteria`
2. Update the JPQL query in `UserRepository`
3. Add tests in a new nested class:

```java
@Nested
@DisplayName("searchUsers: by new criteria")
class SearchByNewCriteria {
    @Test
    void searchUsers_byNewCriteria_returnsExpectedResults() {
        // Test implementation
    }
}
```

## Dependencies (pom.xml)

```xml
<!-- Testcontainers -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>
```

## Summary

This setup provides:

- Real PostgreSQL database testing via Testcontainers
- Comprehensive coverage of complex JPQL queries
- ATDD-compliant test organization
- Clear, maintainable test code using AssertJ
- Fast execution with container reuse and parallel testing
