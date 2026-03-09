# Bean Validation Testing Guide for Spring Boot

## Overview

This guide demonstrates comprehensive testing strategies for Bean Validation (JSR-380) constraints in a Spring Boot application using an Order entity as an example.

## Validation Requirements

The Order entity has the following validation rules:
1. **Order items cannot be empty** - At least one item is required
2. **Total amount must be positive** - Must be greater than zero
3. **Customer email is required and valid** - Must be a valid email format
4. **Order date cannot be in the future** - Must be today or in the past

## Files Included

### Entity Classes
- **Order.java** - Main entity with Bean Validation annotations
- **OrderItem.java** - Nested entity with cascade validation

### Test Classes
- **OrderValidationTest.java** - Unit tests using Validator directly
- **OrderServiceValidationTest.java** - Integration tests at service layer
- **OrderControllerValidationTest.java** - Web layer tests using MockMvc

### Supporting Classes
- **OrderService.java** - Service interface with @Valid annotations
- **OrderController.java** - REST controller with automatic validation
- **GlobalExceptionHandler.java** - Exception handling for validation errors

## Bean Validation Annotations Used

### Order Entity
```java
@NotNull(message = "Order ID is required")
private Long id;

@NotEmpty(message = "Order items cannot be empty")
@Size(min = 1, message = "At least one order item is required")
@Valid
private List<OrderItem> items;

@NotNull(message = "Total amount is required")
@Positive(message = "Total amount must be positive")
private BigDecimal totalAmount;

@NotBlank(message = "Customer email is required")
@Email(message = "Invalid email format")
private String customerEmail;

@NotNull(message = "Order date is required")
@PastOrPresent(message = "Order date cannot be in the future")
private LocalDate orderDate;
```

### OrderItem Entity (Cascade Validation)
```java
@NotNull(message = "Product ID is required")
private Long productId;

@NotBlank(message = "Product name is required")
private String productName;

@NotNull(message = "Quantity is required")
@Positive(message = "Quantity must be positive")
private Integer quantity;

@NotNull(message = "Unit price is required")
@Positive(message = "Unit price must be positive")
private BigDecimal unitPrice;
```

## Testing Approaches

### 1. Unit Testing with Validator (Recommended for pure validation logic)

Directly test validation constraints without Spring context:

```java
@BeforeEach
void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
}

@Test
void shouldFail_WhenOrderItemsIsEmpty() {
    Order order = createValidOrder();
    order.setItems(new ArrayList<>());

    Set<ConstraintViolation<Order>> violations = validator.validate(order);

    assertThat(violations)
        .hasSize(2)
        .extracting(ConstraintViolation::getMessage)
        .contains("Order items cannot be empty", "At least one order item is required");
}
```

**Pros:**
- Fast execution (no Spring context)
- Pure unit tests
- Easy to test edge cases

**Cons:**
- Doesn't test integration with Spring MVC/Service layers

### 2. Service Layer Integration Testing

Test method-level validation with @Validated services:

```java
@SpringBootTest
class OrderServiceValidationTest {
    @Autowired
    private OrderService orderService;

    @Test
    void shouldThrowException_WhenCreatingOrderWithEmptyItems() {
        Order order = createValidOrder();
        order.setItems(List.of());

        assertThatThrownBy(() -> orderService.createOrder(order))
            .isInstanceOf(ConstraintViolationException.class);
    }
}
```

**Pros:**
- Tests Spring's validation integration
- Validates method parameters

**Cons:**
- Slower (requires Spring context)

### 3. Controller Layer Testing with MockMvc

Test REST API validation and error responses:

```java
@WebMvcTest(OrderController.class)
class OrderControllerValidationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn400_WhenOrderItemsIsEmpty() throws Exception {
        Order order = createValidOrder();
        order.setItems(new ArrayList<>());

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").isArray());
    }
}
```

**Pros:**
- Tests HTTP layer integration
- Validates error response format
- Tests JSON serialization/deserialization

**Cons:**
- Slower than pure unit tests
- Requires MockMvc setup

## Test Coverage

### Order Items Validation Tests
- Valid items (pass)
- Null items list (fail)
- Empty items list (fail)
- Invalid nested items (cascade validation)

### Total Amount Validation Tests
- Positive amount (pass)
- Small positive decimal (pass)
- Large positive amount (pass)
- Null amount (fail)
- Zero amount (fail)
- Negative amount (fail)

### Customer Email Validation Tests
- Valid email (pass)
- Email with subdomain (pass)
- Complex email format (pass)
- Null email (fail)
- Empty email (fail)
- Blank email (fail)
- Email without @ symbol (fail)
- Email without domain (fail)
- Email without TLD (fail)

### Order Date Validation Tests
- Today (pass)
- Past date (pass)
- Yesterday (pass)
- One year ago (pass)
- Very old date (pass)
- Null date (fail)
- Tomorrow (fail)
- One week in future (fail)
- One year in future (fail)

## Best Practices

1. **Use Nested Test Classes** - Organize tests by constraint/feature
2. **Create Helper Methods** - Use `createValidOrder()` to reduce duplication
3. **Test Positive and Negative Cases** - Both valid and invalid scenarios
4. **Test Edge Cases** - Boundary values, null, empty, whitespace
5. **Test Cascade Validation** - Verify nested object validation
6. **Test Multiple Violations** - Ensure all errors are collected
7. **Use AssertJ** - More readable assertions
8. **Use Descriptive Test Names** - `@DisplayName` for clarity

## Running the Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=OrderValidationTest
```

### Run with Coverage Report
```bash
mvn test jacoco:report
```

## Dependencies Required

```xml
<dependencies>
    <!-- Bean Validation API -->
    <dependency>
        <groupId>jakarta.validation</groupId>
        <artifactId>jakarta.validation-api</artifactId>
    </dependency>

    <!-- Hibernate Validator (Implementation) -->
    <dependency>
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Common Validation Annotations

| Annotation | Description | Example |
|------------|-------------|---------|
| `@NotNull` | Value must not be null | `@NotNull` |
| `@NotEmpty` | Collection/String must not be empty | `@NotEmpty` |
| `@NotBlank` | String must not be null or whitespace | `@NotBlank` |
| `@Size` | Collection/String size bounds | `@Size(min=1, max=10)` |
| `@Min` | Numeric minimum value | `@Min(0)` |
| `@Max` | Numeric maximum value | `@Max(100)` |
| `@Positive` | Must be positive (> 0) | `@Positive` |
| `@PositiveOrZero` | Must be positive or zero | `@PositiveOrZero` |
| `@Negative` | Must be negative (< 0) | `@Negative` |
| `@Email` | Must be valid email format | `@Email` |
| `@Pattern` | Must match regex pattern | `@Pattern(regexp="...")` |
| `@Past` | Date must be in the past | `@Past` |
| `@PastOrPresent` | Date must be past or now | `@PastOrPresent` |
| `@Future` | Date must be in the future | `@Future` |
| `@FutureOrPresent` | Date must be future or now | `@FutureOrPresent` |
| `@Valid` | Cascade validation to nested object | `@Valid` |

## Key Takeaways

1. **Bean Validation is declarative** - Use annotations on entity fields
2. **Test at multiple layers** - Unit, service, and controller tests
3. **Cascade with @Valid** - Validate nested objects automatically
4. **Customize error messages** - Use the `message` attribute
5. **Handle errors globally** - Use `@RestControllerAdvice` for consistent responses
6. **Test edge cases** - Null, empty, boundary values
7. **Keep tests organized** - Use nested classes and descriptive names
8. **Aim for 100% coverage** - On validation logic at minimum
