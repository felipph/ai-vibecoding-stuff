# User Registration Feature - ATDD Implementation

This project implements a user registration feature for a Spring Boot application following Acceptance Test-Driven Development (ATDD) principles.

## Business Requirements

1. Users must register with email and password
2. Email must be valid and unique
3. Password must be at least 8 characters with at least one uppercase, one lowercase, and one number
4. After successful registration, send a welcome email
5. If email already exists, return an error

## Acceptance Criteria

### Positive Scenarios (Happy Path)

[+] User can register with valid email and strong password
[+] After successful registration, user receives welcome email
[+] User password is stored encrypted in the database

### Negative Scenarios (Validation & Error Cases)

[-] System rejects registration with invalid email format
[-] System rejects registration with existing email (email must be unique)
[-] System rejects registration with password shorter than 8 characters
[-] System rejects registration with password without uppercase letter
[-] System rejects registration with password without lowercase letter
[-] System rejects registration with password without number

## Project Structure

```
outputs/
├── docs/
│   └── acceptance-criteria.md     # Business requirements and test mapping
├── src/
│   ├── main/
│   │   ├── java/com/example/auth/
│   │   │   ├── AuthApplication.java           # Main application
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java        # Security configuration
│   │   │   │   └── GlobalExceptionHandler.java # Exception handling
│   │   │   ├── controller/
│   │   │   │   └── AuthController.java        # REST endpoints
│   │   │   ├── domain/
│   │   │   │   └── User.java                  # User entity
│   │   │   ├── dto/
│   │   │   │   ├── RegisterRequest.java       # Registration DTO
│   │   │   │   └── RegisterResponse.java      # Response DTO
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java        # JPA repository
│   │   │   └── service/
│   │   │       ├── EmailService.java          # Email service interface
│   │   │       ├── EmailServiceImpl.java      # Email implementation
│   │   │       └── UserService.java           # Business logic
│   │   └── resources/
│   │       └── application.yml                # Application configuration
│   └── test/
│       ├── java/com/example/auth/
│       │   ├── controller/
│       │   │   └── AuthControllerTest.java    # @WebMvcTest tests
│       │   ├── dto/
│       │   │   └── RegisterRequestTest.java   # Bean validation tests
│       │   ├── repository/
│       │   │   └── UserRepositoryTest.java    # @DataJpaTest with Testcontainers
│       │   └── service/
│       │       └── UserServiceTest.java       # Mockito tests
│       └── resources/
│           └── application-test.yml           # Test configuration
└── pom.xml                                   # Maven build file
```

## Test Slices Used

### 1. Bean Validation Tests (`RegisterRequestTest`)
- Tests model constraints using `javax.validation.Validator`
- No Spring context required (fast)
- Tests: Email format, password complexity

### 2. Service Layer Tests (`UserServiceTest`)
- Uses `@ExtendWith(MockitoExtension.class)`
- Tests business logic in isolation
- Mocks: UserRepository, PasswordEncoder, EmailService
- Tests: Registration flow, email uniqueness, welcome email sending

### 3. Controller Tests (`AuthControllerTest`)
- Uses `@WebMvcTest(AuthController.class)`
- Tests REST endpoints with MockMvc
- Tests: HTTP status codes, request/response JSON

### 4. Repository Tests (`UserRepositoryTest`)
- Uses `@DataJpaTest` with `@Testcontainers`
- Real PostgreSQL database for integration testing
- Tests: Database queries, unique constraints

## ATDD Workflow Followed

### Step 1: Define Acceptance Criteria
- Captured business requirements in `/docs/acceptance-criteria.md`
- Used `[+]` for positive scenarios and `[-]` for negative scenarios

### Step 2: Write Tests (RED)
- Created test files for each layer:
  - `RegisterRequestTest` for validation
  - `UserServiceTest` for business logic
  - `AuthControllerTest` for REST endpoints
  - `UserRepositoryTest` for database operations

### Step 3: Implement (GREEN)
- Created minimum code to pass tests:
  - Domain model with JPA annotations
  - DTO with Bean Validation constraints
  - Service with business logic
  - Controller with REST endpoints
  - Repository with JPA queries

### Step 4: Refactor
- Applied clean code principles
- Used proper exception handling
- Configured security

## Running the Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run with coverage
mvn test jacoco:report
```

## API Endpoint

### Register User
```
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "ValidPass123"
}

Response (201 Created):
{
  "id": 1,
  "email": "user@example.com",
  "message": "User registered successfully. Welcome email sent."
}

Error Response (409 Conflict):
{
  "code": "EMAIL_ALREADY_EXISTS",
  "message": "Email already registered: user@example.com"
}
```

## FIRST Principles Applied

- **F (Fast)**: Unit tests use mocks, no external services
- **I (Independent)**: Each test is self-contained
- **R (Repeatable)**: Tests produce same results every time
- **S (Self-validating)**: Tests pass/fail automatically
- **T (Timely)**: Tests written before implementation

## Key Design Decisions

1. **Password Encoding**: Uses BCrypt for secure password storage
2. **Email Service**: Interface-based design for easy mocking and future implementation
3. **Validation**: Bean Validation annotations on DTO, not entity
4. **Exception Handling**: Custom exception with proper HTTP status codes
5. **Database**: PostgreSQL with unique constraint on email
6. **Testing Strategy**: Vertical slice testing (each test covers one acceptance criterion)
