# Quick Reference: Testing Bean Validation Constraints

## The ATDD Workflow for Bean Validation

```
1. Write Acceptance Criteria → 2. Write Tests → 3. Add Constraints → 4. Verify
```

## Step-by-Step Process

### 1. Define Acceptance Criteria
```markdown
[+] Order items can have 1 or more items
[-] Order with empty items is rejected with "Order items cannot be empty"
```

### 2. Write the Test
```java
@Test
@DisplayName("[-] should reject order with empty items")
void orderWithEmptyItems_shouldBeInvalid() {
    Order order = Order.builder()
        .items(new ArrayList<>())
        .build();

    Set<ConstraintViolation<Order>> violations = validator.validate(order);

    assertThat(violations).isNotEmpty();
    assertThat(violations.iterator().next().getMessage())
        .isEqualTo("Order items cannot be empty");
}
```

### 3. Add Bean Validation Constraint
```java
@NotNull(message = "Order items cannot be empty")
@Size(min = 1, message = "Order items cannot be empty")
private List<OrderItem> items;
```

### 4. Run Test - Should Pass!
```bash
mvn test -Dtest=OrderTest
```

## Common Bean Validation Annotations

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@NotNull` | Value must not be null | `@NotNull(message = "Field is required")` |
| `@NotBlank` | String must not be null/empty/whitespace | `@NotBlank(message = "Name is required")` |
| `@NotEmpty` | Collection/String must not be empty | `@NotEmpty(message = "List cannot be empty")` |
| `@Size(min, max)` | Collection/String size bounds | `@Size(min=1, message = "At least one item required")` |
| `@Min(value)` | Numeric minimum | `@Min(value = 1, message = "Must be at least 1")` |
| `@Max(value)` | Numeric maximum | `@Max(value = 100, message = "Cannot exceed 100")` |
| `@Positive` | Must be positive (> 0) | `@Positive(message = "Must be positive")` |
| `@PositiveOrZero` | Must be positive or zero | `@PositiveOrZero(message = "Must be non-negative")` |
| `@Email` | Must be valid email format | `@Email(message = "Invalid email")` |
| `@Pattern(regexp)` | Must match regex pattern | `@Pattern(regexp = "^[A-Z]{2}$")` |
| `@Past` | Must be in the past | `@Past(message = "Date must be in the past")` |
| `@PastOrPresent` | Must be past or present | `@PastOrPresent(message = "Cannot be future date")` |
| `@Future` | Must be in the future | `@Future(message = "Date must be future")` |
| `@FutureOrPresent` | Must be future or present | `@FutureOrPresent(message = "Must be future date")` |
| `@DecimalMin(value)` | Decimal minimum | `@DecimalMin("0.01")` |
| `@DecimalMax(value)` | Decimal maximum | `@DecimalMax("9999.99")` |
| `@Digits(integer, fraction)` | Number of digits | `@Digits(integer=5, fraction=2)` |
| `@AssertTrue` | Must be true | `@AssertTrue(message = "Must accept terms")` |
| `@AssertFalse` | Must be false | `@AssertFalse(message = "Must be inactive")` |

## Test Structure Template

```java
@DisplayName("Entity Bean Validation Tests")
class EntityTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Field X Validation")
    class FieldXValidation {

        @Test
        @DisplayName("[+] should accept valid value")
        void validValue_shouldBeValid() {
            Entity entity = Entity.builder()
                .fieldX("valid value")
                .build();

            Set<ConstraintViolation<Entity>> violations = validator.validate(entity);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("[-] should reject invalid value")
        void invalidValue_shouldBeInvalid() {
            Entity entity = Entity.builder()
                .fieldX("invalid value")
                .build();

            Set<ConstraintViolation<Entity>> violations = validator.validate(entity);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                v.getMessage().equals("Expected error message"));
        }
    }
}
```

## AssertJ Assertions for Bean Validation

```java
// Check no violations
assertThat(violations).isEmpty();

// Check has violations
assertThat(violations).isNotEmpty();

// Check violation count
assertThat(violations).hasSize(2);

// Check specific error message exists
assertThat(violations).anyMatch(v ->
    v.getMessage().equals("Expected error message"));

// Check all error messages
List<String> messages = violations.stream()
    .map(ConstraintViolation::getMessage)
    .toList();
assertThat(messages).containsExactlyInAnyOrder(
    "Error 1", "Error 2", "Error 3"
);

// Check property path (which field has error)
assertThat(violations).anyMatch(v ->
    v.getPropertyPath().toString().equals("fieldName"));

// Check invalid value
assertThat(violations).anyMatch(v ->
    v.getInvalidValue().equals("bad value"));
```

## Tips for Good Bean Validation Tests

### ✅ DO
- Test both positive (+) and negative (-) scenarios
- Use descriptive test names: `methodName_scenario_expectedResult`
- Group related tests with `@Nested`
- Use helper methods to create test fixtures
- Verify error messages match business requirements
- Test edge cases (empty, null, boundary values)

### ❌ DON'T
- Don't load Spring context (use `Validator` directly)
- Don't test implementation details
- Don't skip negative scenarios
- Don't use hardcoded "now" dates (use `LocalDate.now()` in tests is OK for `@PastOrPresent`)
- Don't forget to test multiple validation errors simultaneously

## Maven Dependencies (Minimal)

```xml
<dependencies>
    <dependency>
        <groupId>jakarta.validation</groupId>
        <artifactId>jakarta.validation-api</artifactId>
        <version>3.0.2</version>
    </dependency>

    <dependency>
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version>8.0.1.Final</version>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.glassfish.expressly</groupId>
        <artifactId>expressly</artifactId>
        <version>5.0.0</version>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.1</version>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <version>3.24.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=OrderTest

# Run specific nested test class
mvn test -Dtest=OrderTest$OrderItemsValidation

# Run with verbose output
mvn test -Dtest=OrderTest -X
```

## Integration with Spring Boot Controllers

When using `@Valid` in controllers, Spring automatically validates:

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody Order order) {
        // If validation fails, Spring returns 400 Bad Request automatically
        // This method only executes if validation passes
        return ResponseEntity.ok(orderService.create(order));
    }
}
```

Test controller validation with `@WebMvcTest`:

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createOrder_withInvalidData_returns400() throws Exception {
        String invalidOrder = "{\"items\":[],\"totalAmount\":-1}";

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidOrder))
                .andExpect(status().isBadRequest());
    }
}
```

But remember: **Bean Validation unit tests are faster and more precise!**
