# User Registration - Acceptance Criteria

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

## Test Mapping

| Acceptance Criterion | Test Class | Test Method |
|---------------------|------------|-------------|
| [+] User can register with valid email and strong password | `UserServiceTest` | `register_withValidEmailAndStrongPassword_savesUser()` |
| [+] After successful registration, user receives welcome email | `UserServiceTest` | `register_withValidEmailAndStrongPassword_sendsWelcomeEmail()` |
| [+] User password is stored encrypted | `UserServiceTest` | `register_withValidEmailAndStrongPassword_storesEncryptedPassword()` |
| [-] System rejects registration with invalid email format | `RegisterRequestTest` | `validate_withInvalidEmail_failsValidation()` |
| [-] System rejects registration with existing email | `UserServiceTest` | `register_withExistingEmail_throwsException()` |
| [-] System rejects registration with password shorter than 8 characters | `RegisterRequestTest` | `validate_withShortPassword_failsValidation()` |
| [-] System rejects registration with password without uppercase | `RegisterRequestTest` | `validate_withPasswordWithoutUppercase_failsValidation()` |
| [-] System rejects registration with password without lowercase | `RegisterRequestTest` | `validate_withPasswordWithoutLowercase_failsValidation()` |
| [-] System rejects registration with password without number | `RegisterRequestTest` | `validate_withPasswordWithoutNumber_failsValidation()` |

## Test Slices Used

- **Bean Validation Tests**: `RegisterRequestTest` - Test model constraints without Spring context
- **Service Tests**: `UserServiceTest` - Test business logic with Mockito
- **Controller Tests**: `AuthControllerTest` - Test REST endpoints with @WebMvcTest
- **Repository Tests**: `UserRepositoryTest` - Test database operations with @DataJpaTest
