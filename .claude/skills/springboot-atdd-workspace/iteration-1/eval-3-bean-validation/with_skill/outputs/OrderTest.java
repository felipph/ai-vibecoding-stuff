package com.example.order;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bean Validation tests for Order entity.
 *
 * Tests follow ATDD approach:
 * 1. Each test maps to an acceptance criterion
 * 2. Tests use Validator directly (no Spring context needed - FAST)
 * 3. Tests are independent and repeatable (FIRST principles)
 * 4. Tests use AssertJ fluent assertions
 * 5. Tests are grouped by validation concern using @Nested
 */
@DisplayName("Order Bean Validation Tests")
class OrderTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Helper method to create a valid order item
    private OrderItem createValidOrderItem() {
        return OrderItem.builder()
                .productName("Test Product")
                .quantity(1)
                .unitPrice(BigDecimal.TEN)
                .build();
    }

    // Helper method to create a valid order
    private Order.Builder createValidOrderBuilder() {
        return Order.builder()
                .items(List.of(createValidOrderItem()))
                .totalAmount(BigDecimal.valueOf(100.00))
                .customerEmail("customer@example.com")
                .orderDate(LocalDate.now());
    }

    // ==================== ORDER ITEMS VALIDATION ====================

    @Nested
    @DisplayName("Order Items Validation")
    class OrderItemsValidation {

        @Test
        @DisplayName("[+] should accept order with one item")
        void orderWithOneItem_shouldBeValid() {
            // Arrange - Acceptance: [+] Order can be created with one or more items
            Order order = createValidOrderBuilder()
                    .items(List.of(createValidOrderItem()))
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("[+] should accept order with multiple items")
        void orderWithMultipleItems_shouldBeValid() {
            // Arrange - Acceptance: [+] Order can be created with one or more items
            Order order = createValidOrderBuilder()
                    .items(List.of(
                            createValidOrderItem(),
                            createValidOrderItem(),
                            createValidOrderItem()
                    ))
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("[-] should reject order with empty items list")
        void orderWithEmptyItems_shouldBeInvalid() {
            // Arrange - Acceptance: [-] Order with empty items list is rejected
            Order order = createValidOrderBuilder()
                    .items(new ArrayList<>())
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Order items cannot be empty");
        }

        @Test
        @DisplayName("[-] should reject order with null items list")
        void orderWithNullItems_shouldBeInvalid() {
            // Arrange - Acceptance: [-] Order with null items list is rejected
            Order order = createValidOrderBuilder()
                    .items(null)
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().equals("Order items cannot be empty"));
        }
    }

    // ==================== TOTAL AMOUNT VALIDATION ====================

    @Nested
    @DisplayName("Total Amount Validation")
    class TotalAmountValidation {

        @Test
        @DisplayName("[+] should accept order with positive total amount")
        void orderWithPositiveAmount_shouldBeValid() {
            // Arrange - Acceptance: [+] Order can be created with positive total amount
            Order order = createValidOrderBuilder()
                    .totalAmount(BigDecimal.valueOf(100.00))
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("[+] should accept order with very small positive amount")
        void orderWithSmallPositiveAmount_shouldBeValid() {
            // Arrange - Acceptance: [+] Order can be created with positive total amount
            Order order = createValidOrderBuilder()
                    .totalAmount(new BigDecimal("0.01"))
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("[-] should reject order with zero total amount")
        void orderWithZeroAmount_shouldBeInvalid() {
            // Arrange - Acceptance: [-] Order with zero total amount is rejected
            Order order = createValidOrderBuilder()
                    .totalAmount(BigDecimal.ZERO)
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().equals("Total amount must be positive"));
        }

        @Test
        @DisplayName("[-] should reject order with negative total amount")
        void orderWithNegativeAmount_shouldBeInvalid() {
            // Arrange - Acceptance: [-] Order with negative total amount is rejected
            Order order = createValidOrderBuilder()
                    .totalAmount(BigDecimal.valueOf(-10.00))
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().equals("Total amount must be positive"));
        }

        @Test
        @DisplayName("[-] should reject order with null total amount")
        void orderWithNullAmount_shouldBeInvalid() {
            // Arrange - Acceptance: [-] Order with null total amount is rejected
            Order order = createValidOrderBuilder()
                    .totalAmount(null)
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().equals("Total amount is required"));
        }
    }

    // ==================== CUSTOMER EMAIL VALIDATION ====================

    @Nested
    @DisplayName("Customer Email Validation")
    class CustomerEmailValidation {

        @Test
        @DisplayName("[+] should accept order with valid email")
        void orderWithValidEmail_shouldBeValid() {
            // Arrange - Acceptance: [+] Order can be created with valid email address
            Order order = createValidOrderBuilder()
                    .customerEmail("customer@example.com")
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("[+] should accept order with email containing subdomain")
        void orderWithEmailWithSubdomain_shouldBeValid() {
            // Arrange - Acceptance: [+] Order can be created with valid email address
            Order order = createValidOrderBuilder()
                    .customerEmail("user@mail.example.com")
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("[-] should reject order with null email")
        void orderWithNullEmail_shouldBeInvalid() {
            // Arrange - Acceptance: [-] Order with null email is rejected
            Order order = createValidOrderBuilder()
                    .customerEmail(null)
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().equals("Customer email is required"));
        }

        @Test
        @DisplayName("[-] should reject order with empty email")
        void orderWithEmptyEmail_shouldBeInvalid() {
            // Arrange - Acceptance: [-] Order with empty email is rejected
            Order order = createValidOrderBuilder()
                    .customerEmail("")
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().equals("Customer email is required"));
        }

        @Test
        @DisplayName("[-] should reject order with whitespace-only email")
        void orderWithWhitespaceEmail_shouldBeInvalid() {
            // Arrange - Acceptance: [-] Order with empty email is rejected
            Order order = createValidOrderBuilder()
                    .customerEmail("   ")
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().equals("Customer email is required") ||
                    v.getMessage().equals("Customer email must be valid"));
        }

        @Test
        @DisplayName("[-] should reject order with invalid email - missing @")
        void orderWithInvalidEmailNoAt_shouldBeInvalid() {
            // Arrange - Acceptance: [-] Order with invalid email format is rejected
            Order order = createValidOrderBuilder()
                    .customerEmail("customerexample.com")
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().equals("Customer email must be valid"));
        }

        @Test
        @DisplayName("[-] should reject order with invalid email - missing domain")
        void orderWithInvalidEmailNoDomain_shouldBeInvalid() {
            // Arrange - Acceptance: [-] Order with invalid email format is rejected
            Order order = createValidOrderBuilder()
                    .customerEmail("customer@")
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().equals("Customer email must be valid"));
        }

        @Test
        @DisplayName("[-] should reject order with invalid email - missing local part")
        void orderWithInvalidEmailNoLocalPart_shouldBeInvalid() {
            // Arrange - Acceptance: [-] Order with invalid email format is rejected
            Order order = createValidOrderBuilder()
                    .customerEmail("@example.com")
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().equals("Customer email must be valid"));
        }
    }

    // ==================== ORDER DATE VALIDATION ====================

    @Nested
    @DisplayName("Order Date Validation")
    class OrderDateValidation {

        @Test
        @DisplayName("[+] should accept order with today's date")
        void orderWithTodayDate_shouldBeValid() {
            // Arrange - Acceptance: [+] Order can be created with current date
            Order order = createValidOrderBuilder()
                    .orderDate(LocalDate.now())
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("[+] should accept order with past date")
        void orderWithPastDate_shouldBeValid() {
            // Arrange - Acceptance: [+] Order can be created with past date
            Order order = createValidOrderBuilder()
                    .orderDate(LocalDate.now().minusDays(5))
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("[+] should accept order with date from long ago")
        void orderWithOldDate_shouldBeValid() {
            // Arrange - Acceptance: [+] Order can be created with past date
            Order order = createValidOrderBuilder()
                    .orderDate(LocalDate.of(2020, 1, 1))
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("[-] should reject order with future date - tomorrow")
        void orderWithFutureDateTomorrow_shouldBeInvalid() {
            // Arrange - Acceptance: [-] Order with future date is rejected
            Order order = createValidOrderBuilder()
                    .orderDate(LocalDate.now().plusDays(1))
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().equals("Order date cannot be in the future"));
        }

        @Test
        @DisplayName("[-] should reject order with future date - far future")
        void orderWithFutureDateFarFuture_shouldBeInvalid() {
            // Arrange - Acceptance: [-] Order with future date is rejected
            Order order = createValidOrderBuilder()
                    .orderDate(LocalDate.now().plusYears(1))
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().equals("Order date cannot be in the future"));
        }

        @Test
        @DisplayName("[-] should reject order with null date")
        void orderWithNullDate_shouldBeInvalid() {
            // Arrange - Acceptance: [-] Order with null date is rejected
            Order order = createValidOrderBuilder()
                    .orderDate(null)
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().equals("Order date is required"));
        }
    }

    // ==================== INTEGRATION TESTS ====================

    @Nested
    @DisplayName("Multiple Validation Errors")
    class MultipleValidationErrors {

        @Test
        @DisplayName("should report all validation errors when multiple fields are invalid")
        void orderWithMultipleInvalidFields_shouldReportAllErrors() {
            // Arrange - Order with multiple invalid fields
            Order order = Order.builder()
                    .items(new ArrayList<>())  // Empty items
                    .totalAmount(BigDecimal.ZERO)  // Zero amount
                    .customerEmail("invalid-email")  // Invalid email
                    .orderDate(LocalDate.now().plusDays(1))  // Future date
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).hasSize(4); // One for each invalid field

            List<String> messages = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .toList();

            assertThat(messages).containsExactlyInAnyOrder(
                    "Order items cannot be empty",
                    "Total amount must be positive",
                    "Customer email must be valid",
                    "Order date cannot be in the future"
            );
        }

        @Test
        @DisplayName("should accept fully valid order")
        void fullyValidOrder_shouldHaveNoViolations() {
            // Arrange - All fields valid
            Order order = Order.builder()
                    .items(List.of(
                            OrderItem.builder()
                                    .productName("Product 1")
                                    .quantity(2)
                                    .unitPrice(BigDecimal.valueOf(25.00))
                                    .build(),
                            OrderItem.builder()
                                    .productName("Product 2")
                                    .quantity(1)
                                    .unitPrice(BigDecimal.valueOf(50.00))
                                    .build()
                    ))
                    .totalAmount(BigDecimal.valueOf(100.00))
                    .customerEmail("john.doe@example.com")
                    .orderDate(LocalDate.now())
                    .build();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }
    }
}
