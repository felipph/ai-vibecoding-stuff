---
name: springboot-atdd
description: Acceptance Test-Driven Development for Spring Boot applications. Use when user wants to implement features using ATDD (starting with acceptance criteria/business requirements), write Spring Boot tests (Bean Validation, @WebMvcTest, @DataJpaTest, Testcontainers), use AssertJ fluent assertions, or asks about testing Spring Boot services, controllers, or repositories with a business-first approach. Also triggers when user mentions "acceptance criteria", "behavior-driven testing", "FIRST principles", or wants comprehensive test coverage with real databases/integrations.
---

# Spring Boot ATDD — Acceptance Test-Driven Development

## Philosophy

**ATDD = Test-Driven Development starting from business requirements**

Traditional TDD starts with code-level tests. ATDD starts with **acceptance criteria** — business requirements written in collaboration with stakeholders — and works backward to implementation.

**Why ATDD?**
- **Business-first**: Tests map directly to acceptance criteria, ensuring you build what stakeholders actually want
- **Clearer communication**: Acceptance criteria create shared language between developers, QA, and business
- **Comprehensive coverage**: By testing both success and failure scenarios from business requirements, you naturally get better coverage
- **Reduced over-engineering**: Focus on what's needed, not what might be nice

## The ATDD Workflow

```
Acceptance Criteria → Tests → Implementation → Refactor
        ↓                ↓           ↓            ↓
   Business        Behavior    Minimal Code   Clean Up
   Requirements    Tests       to Pass       (tests pass)
```

### Step 1: Define Acceptance Criteria

Before writing code, capture what the feature should do in business terms:

```gherkin
[+] Talent can request password reset by entering registered email
[-] If email is invalid or not registered, system displays error message
[+] If email is valid and registered, system sends password reset link to that email
[+] User can create new password using the reset link
[-] If token is expired or invalid, system displays error message
[+] If password is valid and confirmation matches, system saves new password
```

**Criteria format:**
- `[+]` for positive/happy path scenarios
- `[-]` for negative/edge case scenarios
- Concrete and testable
- Understood by non-technical stakeholders

### Step 2: Write Tests from Acceptance Criteria

Each acceptance criterion becomes one or more tests. Use **behavior-focused naming**:

```java
@Test
void requestPasswordReset_withValidEmail_sendsResetLink() {
    // Test for: [+] If email is valid and registered, system sends reset link
}

@Test
void requestPasswordReset_withUnregisteredEmail_throwsException() {
    // Test for: [-] If email is invalid or not registered, system displays error
}

@Test
void resetPasswordWithToken_withValidToken_updatesPasswordAndMarksTokenUsed() {
    // Test for: [+] If password is valid and confirmation matches, system saves it
}

@Test
void resetPasswordWithToken_withInvalidToken_throwsException() {
    // Test for: [-] If token is expired or invalid, system displays error
}
```

**Naming convention:** `methodName_scenario_expectedResult`

### Step 3: Write Minimum Code to Pass

Implement just enough to make tests pass. Don't anticipate future tests — one test at a time.

### Step 4: Refactor with Tests Protecting You

Once tests pass, improve structure while keeping them green.

## Spring Boot Testing Patterns

### FIRST Principles

Good tests in Spring Boot follow FIRST:

- **F — Fast**: Use in-memory databases, mocks for external services
- **I — Independent**: Each test is self-contained, no shared state
- **R — Repeatable**: Same results every time, no date/time randomness
- **S — Self-validating**: Tests pass/fail automatically, no manual inspection
- **T — Timely**: Write tests BEFORE implementation code

### 1. Bean Validation Testing

Test model constraints using `javax.validation.Validator`:

```java
class ExperienceTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testCompanyEmpty() {
        User user = createUser();

        Experience experience = Experience.builder()
                .title("Software Engineer")
                .company("") // Empty company name
                .startDate(LocalDate.of(2020, 5, 1))
                .user(user)
                .build();

        Set<ConstraintViolation<Experience>> violations = validator.validate(experience);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Company is required")));
    }

    private User createUser() {
        return User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();
    }
}
```

**Key patterns:**
- Use `Validator` directly instead of loading Spring context
- Create test fixtures (`createUser()`) for readability
- Test both constraint existence and message content
- Use `@Nested` classes to group related tests

### 2. Service Layer Testing with Mockito

Test business logic with `@ExtendWith(MockitoExtension.class)`:

```java
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void resetPasswordWithToken_withValidToken_updatesPasswordAndMarksTokenUsed() {
        // Arrange
        String token = "abc";
        String newPassword = "NewPass123#";
        String encodedPassword = "encoded_password";
        String email = "user@example.com";

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .email(email)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        User user = new User();
        user.setEmail(email);

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        // Act
        authenticationService.resetPasswordWithToken(token, newPassword);

        // Assert
        verify(userRepository).save(user);
        verify(tokenRepository).save(resetToken);
        assertThat(user.getPassword()).isEqualTo(encodedPassword);
        assertThat(resetToken.isUsed()).isTrue();
    }

    @Test
    void resetPasswordWithToken_withInvalidToken_throwsException() {
        // Arrange
        when(tokenRepository.findByToken("notfound")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.resetPasswordWithToken("notfound", "pw"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token tidak valid");
    }
}
```

**Key patterns:**
- **Arrange-Act-Assert**: Clear structure
- **Fluent assertions** (AssertJ): More readable than JUnit assertions
- **Behavior verification**: Use `verify()` to check interactions
- **Exception testing**: Use `assertThatThrownBy()`

### 3. Controller Testing with @WebMvcTest

Test REST endpoints with `@WebMvcTest`:

```java
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authService;

    @Test
    void requestPasswordReset_withValidEmail_returnsSuccess() throws Exception {
        // Arrange
        when(authService.requestPasswordReset("user@example.com"))
                .thenReturn("Password reset link sent");

        // Act & Assert
        mockMvc.perform(post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"user@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset link sent"));
    }

    @Test
    void requestPasswordReset_withInvalidEmail_returnsBadRequest() throws Exception {
        // Arrange
        when(authService.requestPasswordReset("invalid@example.com"))
                .thenThrow(new IllegalArgumentException("Email not registered"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"invalid@example.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email not registered"));
    }
}
```

### 4. Repository Testing with @DataJpaTest

Test database operations with `@DataJpaTest` and Testcontainers for real databases:

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

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_withExistingEmail_returnsUser() {
        // Arrange
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();
        userRepository.save(user);

        // Act
        Optional<User> found = userRepository.findByEmail("john@example.com");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findByEmail_withNonExistentEmail_returnsEmpty() {
        // Act
        Optional<User> found = userRepository.findByEmail("notfound@example.com");

        // Assert
        assertThat(found).isEmpty();
    }
}
```

**Key patterns:**
- Use `@Testcontainers` for real databases (not H2) in integration tests
- Tests are slower but verify real database behavior
- Perfect for testing complex queries, constraints, and JPA mappings

## ATDD Workflow Step-by-Step

### Phase 1: Planning — Define Acceptance Criteria

Before any code, work with the user to define acceptance criteria in business language:

**Ask the user:**
> "What should this feature do? Let's write acceptance criteria in the format: `[+] for what should happen` and `[-] for what should fail`."

**Example output:**
```
[+] User can register with valid email and strong password
[-] System rejects registration with existing email
[-] System rejects registration with weak password (< 8 chars, no number)
[+] After registration, user receives confirmation email
```

### Phase 2: Write Tests (RED)

For each acceptance criterion, write a test:

1. **Choose the right test slice:**
   - Model validation? → `Validator` test
   - Business logic? → `@ExtendWith(MockitoExtension.class)`
   - REST endpoint? → `@WebMvcTest`
   - Database query? → `@DataJpaTest` with Testcontainers

2. **Use Arrange-Act-Assert pattern:**
   ```java
   @Test
   void methodName_scenario_expectedResult() {
       // Arrange - set up test data and mocks
       // Act - call the method being tested
       // Assert - verify expected outcome
   }
   ```

3. **Use AssertJ fluent assertions:**
   - `assertThat(actual).isEqualTo(expected)`
   - `assertThat(list).hasSize(3)`
   - `assertThatThrownBy(() -> method()).isInstanceOf(Exception.class)`

### Phase 3: Implement (GREEN)

Write **minimum code** to pass the test:

```java
// BAD: Anticipating future tests
public User register(String email, String password) {
    if (userRepository.existsByEmail(email)) {
        throw new EmailAlreadyExistsException();
    }
    if (!isStrongPassword(password)) {
        throw new WeakPasswordException();
    }
    // Sending email (not tested yet!)
    emailService.sendWelcomeEmail(email);
    // ...
}

// GOOD: Just enough for THIS test
public User register(String email, String password) {
    if (userRepository.existsByEmail(email)) {
        throw new EmailAlreadyExistsException();
    }
    User user = new User(email, passwordEncoder.encode(password));
    return userRepository.save(user);
}
```

### Phase 4: Refactor

Once tests pass, clean up the code while keeping tests green:

- Extract methods for clarity
- Apply design patterns where natural
- Remove duplication
- **Run tests after each refactor step**

## Anti-Patterns to Avoid

### ❌ Horizontal Slicing

**WRONG:**
```
RED:   Write all tests (test1, test2, test3, test4, test5)
GREEN: Write all implementation (impl1, impl2, impl3, impl4, impl5)
```

This produces tests that verify **imagined behavior**, not actual behavior.

**RIGHT (vertical slicing):**
```
RED→GREEN: test1 → impl1
RED→GREEN: test2 → impl2
RED→GREEN: test3 → impl3
```

Each test responds to what you learned from the previous cycle.

### ❌ Testing Implementation Details

**WRONG:**
```java
verify(userRepository).save(userCaptor.capture()); // Tests HOW it works
assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded");
```

**RIGHT:**
```java
assertThat(authenticationService.register("email", "pass")).isNotNull(); // Tests WHAT it does
```

### ❌ Skipping Acceptance Criteria

**WRONG:** "I know what to build, let me just write the code."

**RIGHT:** Always start with acceptance criteria, even for simple features. They clarify requirements and prevent over-engineering.

## Checklist for Each Test

```
[ ] Test name clearly describes behavior (method_scenario_expectedResult)
[ ] Test maps to an acceptance criterion
[ ] Arrange-Act-Assert structure is clear
[ ] Test uses public interface only (no private method testing)
[ ] Test uses appropriate test slice (Validator, Mockito, @WebMvcTest, @DataJpaTest)
[ ] Assertions use AssertJ fluent style
[ ] Test would survive refactoring (behavior-focused, not implementation-focused)
[ ] Test is independent (can run alone or in any order)
[ ] Test uses fixed data (no randomness, no hardcoded dates like "now")
```

## When to Use This Skill

Use **Spring Boot ATDD** when:
- User wants to implement a feature with business requirements
- User mentions "acceptance criteria", "behavior-driven development", or "ATDD"
- User wants to write Spring Boot tests (controllers, services, repositories)
- User asks about Bean Validation testing
- User wants comprehensive test coverage with real databases (Testcontainers)
- User wants to use AssertJ fluent assertions
- User cares about FIRST principles for good tests

For general TDD philosophy (behavior vs implementation testing), use the `tdd` skill instead.
