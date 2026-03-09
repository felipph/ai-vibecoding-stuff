# Password Reset Feature Implementation

This implementation provides a secure password reset feature using Test-Driven Development (TDD) with proper test naming and structure.

## Acceptance Criteria Coverage

- [x] User can request password reset with registered email
- [x] If email is not registered, show generic success message (security)
- [x] System sends reset token valid for 1 hour
- [x] User can reset password with valid token
- [x] Expired tokens are rejected
- [x] Used tokens cannot be reused

## Architecture

### Domain Layer (Business Logic)

**Models:**
- `PasswordResetToken` - Domain entity representing a reset token
- `User` - Domain entity representing a user

**Services:**
- `PasswordResetService` - Main service handling reset operations
- `EmailService` - Interface for email operations

**Repositories (Interfaces):**
- `PasswordResetTokenRepository` - Repository interface for tokens
- `UserRepository` - Repository interface for users

**Exceptions:**
- `InvalidTokenException` - Thrown when token is invalid
- `TokenExpiredException` - Thrown when token has expired
- `TokenUsedException` - Thrown when token has already been used

### Application Layer (REST API)

**Controllers:**
- `PasswordResetController` - REST endpoints for password reset

**DTOs:**
- `PasswordResetRequest` - Request DTO for reset request
- `PasswordResetConfirmRequest` - Request DTO for reset confirmation
- `TokenValidateRequest` - Request DTO for token validation

**Exception Handling:**
- `GlobalExceptionHandler` - Converts domain exceptions to HTTP responses

### Infrastructure Layer (Persistence & Integration)

**Persistence:**
- `PasswordResetTokenEntity` - JPA entity
- `PasswordResetTokenJpaRepository` - Spring Data JPA repository
- `PasswordResetTokenRepositoryImpl` - Repository implementation

**Email:**
- `SmtEmailService` - Email service implementation using JavaMail

**Scheduler:**
- `ExpiredTokenCleanupJob` - Scheduled job to clean up expired tokens

## Test Structure

### Unit Tests

1. **PasswordResetServiceTest** - Unit tests for the main service
   - Tests all acceptance criteria
   - Uses mocks for dependencies
   - Organized with nested test classes

2. **PasswordResetTokenTest** - Unit tests for the domain model
   - Tests token generation, expiration, and validity

3. **PasswordResetTokenRepositoryImplTest** - Unit tests for repository implementation

### Integration Tests

1. **PasswordResetControllerIntegrationTest** - Integration tests for REST API
   - Tests complete HTTP request/response flow
   - Uses MockMvc
   - Tests all endpoints

## Test Naming Convention

Tests follow the pattern: `should_expectedBehavior_when_condition`

Examples:
- `shouldCreateAndSendResetTokenForRegisteredEmail`
- `shouldRejectExpiredToken`
- `shouldReturn400ForExpiredToken`

## API Endpoints

### POST /api/auth/password-reset/request
Request a password reset for an email.

**Request:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "message": "If the email exists, a reset link has been sent"
}
```

### POST /api/auth/password-reset/confirm
Confirm password reset with token and new password.

**Request:**
```json
{
  "token": "uuid-token-value",
  "newPassword": "newSecurePassword123"
}
```

**Response:**
```json
{
  "message": "Password has been successfully reset"
}
```

### POST /api/auth/password-reset/validate
Validate if a token is valid.

**Request:**
```json
{
  "token": "uuid-token-value"
}
```

**Response:**
```json
{
  "valid": true
}
```

## Security Features

1. **Generic Success Message** - Same response whether email exists or not (prevents email enumeration)
2. **Token Expiration** - Tokens expire after 1 hour
3. **Single Use** - Tokens can only be used once
4. **Token Invalidation** - Previous tokens invalidated when requesting new one
5. **Password Validation** - Minimum 8 characters, maximum 100

## Database Schema

```sql
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(100) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Configuration

Add to `application.properties`:

```properties
# Token Settings
password.reset.token.validity-hours=1
password.reset.token.cleanup.enabled=true
password.reset.token.cleanup.cron=0 0 * * * *

# Email Settings
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}

# Password Reset URL
password.reset.base-url=https://example.com/reset-password
```

## Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=PasswordResetServiceTest

# Run with coverage
./mvnw test jacoco:report
```

## TDD Approach

This implementation follows the TDD cycle:

1. **Red** - Write a failing test first
2. **Green** - Write minimal code to make the test pass
3. **Refactor** - Improve code while keeping tests green

Each acceptance criterion was implemented following this cycle, resulting in comprehensive test coverage and well-structured code.

## Files Generated

### Domain Layer
- PasswordResetToken.java
- User.java
- PasswordResetService.java
- EmailService.java
- UserRepository.java
- PasswordResetTokenRepository.java
- InvalidTokenException.java
- TokenExpiredException.java
- TokenUsedException.java

### Application Layer
- PasswordResetController.java
- GlobalExceptionHandler.java
- PasswordResetRequest.java
- PasswordResetConfirmRequest.java
- TokenValidateRequest.java

### Infrastructure Layer
- PasswordResetTokenEntity.java
- PasswordResetTokenJpaRepository.java
- PasswordResetTokenRepositoryImpl.java
- SmtEmailService.java
- ExpiredTokenCleanupJob.java

### Tests
- PasswordResetServiceTest.java
- PasswordResetTokenTest.java
- PasswordResetTokenRepositoryImplTest.java
- PasswordResetControllerIntegrationTest.java

### Configuration
- V001__Create_Password_Reset_Tokens_Table.sql
- application-password-reset.properties
