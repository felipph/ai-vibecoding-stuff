package com.example.order.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Parameterized tests for Order entity validation.
 * These tests use JUnit 5's parameterized test feature to test multiple scenarios.
 */
@DisplayName("Order Validation Parameterized Tests")
class OrderValidationParameterizedTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Order createValidOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setItems(List.of(
            new OrderItem(1L, "Product 1", 2, new BigDecimal("10.00"))
        ));
        order.setTotalAmount(new BigDecimal("20.00"));
        order.setCustomerEmail("customer@example.com");
        order.setOrderDate(LocalDate.now());
        return order;
    }

    // ============================================
    // Email Validation Parameterized Tests
    // ============================================

    @DisplayName("Should accept valid email formats")
    @ParameterizedTest(name = "Email: {0}")
    @ValueSource(strings = {
        "user@example.com",
        "user.name@example.com",
        "user+tag@example.com",
        "user@subdomain.example.com",
        "user@example.co.uk",
        "user123@example.io",
        "a@b.co",
        "user_name@example.org",
        "user-name@example.net"
    })
    void shouldAcceptValidEmailFormats(String email) {
        // Arrange
        Order order = createValidOrder();
        order.setCustomerEmail(email);

        // Act
        Set<ConstraintViolation<Order>> violations = validator.validate(order);

        // Assert
        assertThat(violations).isEmpty();
    }

    @DisplayName("Should reject invalid email formats")
    @ParameterizedTest(name = "Email: {0}")
    @ValueSource(strings = {
        "",
        "   ",
        "plaintext",
        "@example.com",
        "user@",
        "user@example",
        "user@example.",
        "user @example.com",
        "user@exam ple.com",
        "user@@example.com"
    })
    void shouldRejectInvalidEmailFormats(String email) {
        // Arrange
        Order order = createValidOrder();
        order.setCustomerEmail(email);

        // Act
        Set<ConstraintViolation<Order>> violations = validator.validate(order);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream()
            .map(ConstraintViolation::getMessage)
            .toList())
            .containsAnyOf(
                "Customer email is required",
                "Invalid email format"
            );
    }

    // ============================================
    // Total Amount Validation Parameterized Tests
    // ============================================

    @DisplayName("Should accept positive total amounts")
    @ParameterizedTest(name = "Amount: {0}")
    @ValueSource(strings = {
        "0.01",
        "1.00",
        "10.50",
        "100.00",
        "1000.99",
        "999999.99",
        "0.99"
    })
    void shouldAcceptPositiveAmounts(String amount) {
        // Arrange
        Order order = createValidOrder();
        order.setTotalAmount(new BigDecimal(amount));

        // Act
        Set<ConstraintViolation<Order>> violations = validator.validate(order);

        // Assert
        assertThat(violations).isEmpty();
    }

    @DisplayName("Should reject non-positive total amounts")
    @ParameterizedTest(name = "Amount: {0}")
    @ValueSource(strings = {
        "0",
        "-0.01",
        "-1.00",
        "-100.00",
        "-999999.99"
    })
    void shouldRejectNonPositiveAmounts(String amount) {
        // Arrange
        Order order = createValidOrder();
        order.setTotalAmount(new BigDecimal(amount));

        // Act
        Set<ConstraintViolation<Order>> violations = validator.validate(order);

        // Assert
        assertThat(violations)
            .hasSize(1)
            .extracting(ConstraintViolation::getMessage)
            .contains("Total amount must be positive");
    }

    // ============================================
    // Order Date Validation Parameterized Tests
    // ============================================

    @DisplayName("Should accept past or present order dates")
    @ParameterizedTest(name = "Date offset: {0} days from now")
    @ValueSource(longs = {
        0,      // Today
        1,      // Yesterday
        7,      // One week ago
        30,     // One month ago
        365,    // One year ago
        730,    // Two years ago
        1825    // Five years ago
    })
    void shouldAcceptPastOrPresentDates(long daysAgo) {
        // Arrange
        Order order = createValidOrder();
        order.setOrderDate(LocalDate.now().minusDays(daysAgo));

        // Act
        Set<ConstraintViolation<Order>> violations = validator.validate(order);

        // Assert
        assertThat(violations).isEmpty();
    }

    @DisplayName("Should reject future order dates")
    @ParameterizedTest(name = "Date offset: {0} days in the future")
    @ValueSource(longs = {
        1,      // Tomorrow
        2,      // Day after tomorrow
        7,      // One week ahead
        30,     // One month ahead
        365     // One year ahead
    })
    void shouldRejectFutureDates(long daysAhead) {
        // Arrange
        Order order = createValidOrder();
        order.setOrderDate(LocalDate.now().plusDays(daysAhead));

        // Act
        Set<ConstraintViolation<Order>> violations = validator.validate(order);

        // Assert
        assertThat(violations)
            .hasSize(1)
            .extracting(ConstraintViolation::getMessage)
            .contains("Order date cannot be in the future");
    }

    // ============================================
    // Complex Scenarios with MethodSource
    // ============================================

    static Stream<Arguments> invalidOrderScenarios() {
        return Stream.of(
            Arguments.of(
                "Empty items",
                List.of(),
                "Order items cannot be empty"
            ),
            Arguments.of(
                "Null items",
                null,
                "Order items cannot be empty"
            )
        );
    }

    @DisplayName("Should detect invalid order items scenarios")
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidOrderScenarios")
    void shouldDetectInvalidOrderItems(
            String scenario,
            List<OrderItem> items,
            String expectedMessage) {
        // Arrange
        Order order = createValidOrder();
        order.setItems(items);

        // Act
        Set<ConstraintViolation<Order>> violations = validator.validate(order);

        // Assert
        assertThat(violations)
            .isNotEmpty()
            .extracting(ConstraintViolation::getMessage)
            .contains(expectedMessage);
    }

    static Stream<Arguments> multipleValidationErrorsScenarios() {
        return Stream.of(
            Arguments.of(
                "All fields invalid - scenario 1",
                new ArrayList<OrderItem>(),
                new BigDecimal("-10.00"),
                "invalid",
                LocalDate.now().plusDays(1),
                5  // Expected number of violations
            ),
            Arguments.of(
                "All fields invalid - scenario 2",
                null,
                BigDecimal.ZERO,
                "",
                LocalDate.now().plusMonths(1),
                5
            ),
            Arguments.of(
                "All fields invalid - scenario 3",
                new ArrayList<OrderItem>(),
                new BigDecimal("-0.01"),
                "   ",
                LocalDate.now().plusYears(1),
                5
            )
        );
    }

    @DisplayName("Should detect multiple validation errors")
    @ParameterizedTest(name = "{0}")
    @MethodSource("multipleValidationErrorsScenarios")
    void shouldDetectMultipleValidationErrors(
            String scenario,
            List<OrderItem> items,
            BigDecimal totalAmount,
            String customerEmail,
            LocalDate orderDate,
            int expectedViolationCount) {
        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setItems(items);
        order.setTotalAmount(totalAmount);
        order.setCustomerEmail(customerEmail);
        order.setOrderDate(orderDate);

        // Act
        Set<ConstraintViolation<Order>> violations = validator.validate(order);

        // Assert
        assertThat(violations).hasSize(expectedViolationCount);

        List<String> messages = violations.stream()
            .map(ConstraintViolation::getMessage)
            .toList();

        // Verify specific error messages are present
        assertThat(messages).containsAnyOf(
            "Order items cannot be empty",
            "At least one order item is required",
            "Total amount must be positive",
            "Customer email is required",
            "Invalid email format",
            "Order date cannot be in the future"
        );
    }

    // ============================================
    // OrderItem Cascade Validation
    // ============================================

    static Stream<Arguments> invalidOrderItemScenarios() {
        return Stream.of(
            Arguments.of(
                "Null product ID",
                null,
                "Product Name",
                1,
                new BigDecimal("10.00"),
                "Product ID is required"
            ),
            Arguments.of(
                "Blank product name",
                1L,
                "",
                1,
                new BigDecimal("10.00"),
                "Product name is required"
            ),
            Arguments.of(
                "Zero quantity",
                1L,
                "Product Name",
                0,
                new BigDecimal("10.00"),
                "Quantity must be positive"
            ),
            Arguments.of(
                "Negative quantity",
                1L,
                "Product Name",
                -1,
                new BigDecimal("10.00"),
                "Quantity must be positive"
            ),
            Arguments.of(
                "Zero unit price",
                1L,
                "Product Name",
                1,
                BigDecimal.ZERO,
                "Unit price must be positive"
            ),
            Arguments.of(
                "Negative unit price",
                1L,
                "Product Name",
                1,
                new BigDecimal("-10.00"),
                "Unit price must be positive"
            )
        );
    }

    @DisplayName("Should cascade validate order items")
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidOrderItemScenarios")
    void shouldCascadeValidateOrderItems(
            String scenario,
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            String expectedMessage) {
        // Arrange
        Order order = createValidOrder();
        order.setItems(List.of(
            new OrderItem(productId, productName, quantity, unitPrice)
        ));

        // Act
        Set<ConstraintViolation<Order>> violations = validator.validate(order);

        // Assert
        assertThat(violations)
            .isNotEmpty()
            .extracting(ConstraintViolation::getMessage)
            .contains(expectedMessage);
    }
}
