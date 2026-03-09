# Order Bean Validation Tests - Implementation Summary

## Overview

This implementation demonstrates **Acceptance Test-Driven Development (ATDD)** for testing Bean Validation constraints in a Spring Boot application.

## Files Created

### 1. acceptance-criteria.md
Business requirements written in ATDD format using `[+]` for positive scenarios and `[-]` for negative scenarios. These criteria drive the test creation.

### 2. Order.java
The Order entity with Bean Validation constraints:
- `@NotNull`, `@Size(min=1)` for items validation
- `@NotNull`, `@Positive` for total amount validation
- `@NotNull`, `@NotBlank`, `@Email` for customer email validation
- `@NotNull`, `@PastOrPresent` for order date validation

### 3. OrderItem.java
Supporting entity representing items in an order.

### 4. OrderTest.java
Comprehensive test suite following ATDD principles with:
- **27 test cases** covering all acceptance criteria
- **@Nested test classes** grouping related tests by validation concern
- **AssertJ fluent assertions** for better readability
- **FIRST principles** compliance (Fast, Independent, Repeatable, Self-validating, Timely)
- **Direct Validator usage** (no Spring context - tests are fast!)

## ATDD Approach Applied

### Step 1: Define Acceptance Criteria
```
[+] Order can be created with one or more items
[-] Order with empty items list is rejected
[-] Order with null items list is rejected
... (see acceptance-criteria.md for full list)
```

### Step 2: Write Tests from Criteria
Each acceptance criterion becomes one or more test methods:
- `orderWithOneItem_shouldBeValid()` → tests `[+]` criterion
- `orderWithEmptyItems_shouldBeInvalid()` → tests `[-]` criterion

### Step 3: Implementation with Bean Validation
Entity uses standard Jakarta Bean Validation annotations to satisfy tests.

### Step 4: Refactor
Tests are well-organized with:
- Helper methods (`createValidOrderBuilder()`, `createValidOrderItem()`)
- Builder pattern for test data construction
- Clear naming convention: `methodName_scenario_expectedResult`

## Test Coverage

### Order Items Validation (4 tests)
- ✅ Valid: one item, multiple items
- ❌ Invalid: empty list, null list

### Total Amount Validation (5 tests)
- ✅ Valid: positive amounts (including small decimals)
- ❌ Invalid: zero, negative, null

### Customer Email Validation (9 tests)
- ✅ Valid: standard email, subdomain email
- ❌ Invalid: null, empty, whitespace, missing @, missing domain, missing local part

### Order Date Validation (6 tests)
- ✅ Valid: today, past dates
- ❌ Invalid: future dates (tomorrow, far future), null

### Integration Tests (2 tests)
- Multiple validation errors simultaneously
- Fully valid order

## Running the Tests

### Prerequisites
```xml
<dependencies>
    <!-- Jakarta Bean Validation API -->
    <dependency>
        <groupId>jakarta.validation</groupId>
        <artifactId>jakarta.validation-api</artifactId>
        <version>3.0.2</version>
    </dependency>

    <!-- Hibernate Validator (implementation) -->
    <dependency>
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version>8.0.1.Final</version>
        <scope>test</scope>
    </dependency>

    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.1</version>
        <scope>test</scope>
    </dependency>

    <!-- AssertJ -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <version>3.24.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Test Execution
```bash
mvn test -Dtest=OrderTest
```

## Key Patterns Demonstrated

### 1. Direct Validator Usage (Fast Tests)
```java
@BeforeAll
static void setupValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
}
```
No Spring context needed - tests run in milliseconds!

### 2. Nested Test Classes (Organization)
```java
@Nested
@DisplayName("Order Items Validation")
class OrderItemsValidation {
    // All tests related to items validation
}
```

### 3. Builder Pattern (Test Readability)
```java
Order order = Order.builder()
    .items(List.of(createValidOrderItem()))
    .totalAmount(BigDecimal.valueOf(100.00))
    .customerEmail("customer@example.com")
    .orderDate(LocalDate.now())
    .build();
```

### 4. AssertJ Fluent Assertions
```java
assertThat(violations).isNotEmpty();
assertThat(violations).anyMatch(v ->
    v.getMessage().equals("Order items cannot be empty"));
```

### 5. Test Naming Convention
`methodName_scenario_expectedResult`
Example: `orderWithEmptyItems_shouldBeInvalid`

## Benefits of This Approach

1. **Business-First**: Tests directly map to acceptance criteria
2. **Fast Execution**: No Spring context, pure unit tests
3. **Comprehensive Coverage**: Both positive and negative scenarios
4. **Self-Documenting**: Test names and @DisplayName explain business rules
5. **Maintainable**: Nested classes and helper methods keep tests organized
6. **FIRST Principles**: Tests are Fast, Independent, Repeatable, Self-validating, Timely

## Alternative: Using @Valid in Controllers

For integration-level validation testing, you can also test that Spring MVC automatically validates `@Valid` parameters:

```java
@WebMvcTest(OrderController.class)
class OrderControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createOrder_withInvalidData_returnsBadRequest() throws Exception {
        String invalidOrder = "{}"; // Empty JSON

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidOrder))
                .andExpect(status().isBadRequest());
    }
}
```

However, the Bean Validation unit tests shown here are:
- **Faster** (no Spring context)
- **More precise** (test specific constraint violations)
- **Better for TDD** (immediate feedback loop)

## Conclusion

This implementation demonstrates how to test Bean Validation constraints using ATDD principles. By starting with acceptance criteria and writing tests first, we ensure comprehensive coverage of business requirements while maintaining fast, independent, and readable tests.
