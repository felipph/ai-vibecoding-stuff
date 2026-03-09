package com.example.order.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
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
 * Comprehensive tests for Order entity Bean Validation constraints.
 *
 * Tests cover:
 * - Order items cannot be empty
 * - Total amount must be positive
 * - Customer email is required and valid
 * - Order date cannot be in the future
 */
@DisplayName("Order Bean Validation Tests")
class OrderValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Helper method to create a valid order for testing.
     */
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

    @Nested
    @DisplayName("Order Items Validation")
    class OrderItemsValidation {

        @Test
        @DisplayName("Should pass validation when order has at least one item")
        void shouldPass_WhenOrderHasItems() {
            // Arrange
            Order order = createValidOrder();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation when order items list is null")
        void shouldFail_WhenOrderItemsIsNull() {
            // Arrange
            Order order = createValidOrder();
            order.setItems(null);

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Order items cannot be empty");
        }

        @Test
        @DisplayName("Should fail validation when order items list is empty")
        void shouldFail_WhenOrderItemsIsEmpty() {
            // Arrange
            Order order = createValidOrder();
            order.setItems(new ArrayList<>());

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations)
                .hasSize(2)
                .extracting(ConstraintViolation::getMessage)
                .contains("Order items cannot be empty", "At least one order item is required");
        }

        @Test
        @DisplayName("Should cascade validate order items")
        void shouldCascadeValidate_WhenOrderItemIsInvalid() {
            // Arrange
            Order order = createValidOrder();
            order.setItems(List.of(
                new OrderItem(null, "", -1, new BigDecimal("-10.00"))
            ));

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList())
                .contains(
                    "Product ID is required",
                    "Product name is required",
                    "Quantity must be positive",
                    "Unit price must be positive"
                );
        }
    }

    @Nested
    @DisplayName("Total Amount Validation")
    class TotalAmountValidation {

        @Test
        @DisplayName("Should pass validation when total amount is positive")
        void shouldPass_WhenTotalAmountIsPositive() {
            // Arrange
            Order order = createValidOrder();
            order.setTotalAmount(new BigDecimal("100.50"));

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when total amount is a small positive decimal")
        void shouldPass_WhenTotalAmountIsSmallPositiveDecimal() {
            // Arrange
            Order order = createValidOrder();
            order.setTotalAmount(new BigDecimal("0.01"));

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation when total amount is null")
        void shouldFail_WhenTotalAmountIsNull() {
            // Arrange
            Order order = createValidOrder();
            order.setTotalAmount(null);

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Total amount is required");
        }

        @Test
        @DisplayName("Should fail validation when total amount is zero")
        void shouldFail_WhenTotalAmountIsZero() {
            // Arrange
            Order order = createValidOrder();
            order.setTotalAmount(BigDecimal.ZERO);

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Total amount must be positive");
        }

        @Test
        @DisplayName("Should fail validation when total amount is negative")
        void shouldFail_WhenTotalAmountIsNegative() {
            // Arrange
            Order order = createValidOrder();
            order.setTotalAmount(new BigDecimal("-10.00"));

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Total amount must be positive");
        }
    }

    @Nested
    @DisplayName("Customer Email Validation")
    class CustomerEmailValidation {

        @Test
        @DisplayName("Should pass validation when customer email is valid")
        void shouldPass_WhenCustomerEmailIsValid() {
            // Arrange
            Order order = createValidOrder();
            order.setCustomerEmail("customer@example.com");

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when email has subdomain")
        void shouldPass_WhenEmailHasSubdomain() {
            // Arrange
            Order order = createValidOrder();
            order.setCustomerEmail("user@mail.example.com");

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation when customer email is null")
        void shouldFail_WhenCustomerEmailIsNull() {
            // Arrange
            Order order = createValidOrder();
            order.setCustomerEmail(null);

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Customer email is required");
        }

        @Test
        @DisplayName("Should fail validation when customer email is empty")
        void shouldFail_WhenCustomerEmailIsEmpty() {
            // Arrange
            Order order = createValidOrder();
            order.setCustomerEmail("");

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Customer email is required");
        }

        @Test
        @DisplayName("Should fail validation when customer email is blank")
        void shouldFail_WhenCustomerEmailIsBlank() {
            // Arrange
            Order order = createValidOrder();
            order.setCustomerEmail("   ");

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Customer email is required");
        }

        @Test
        @DisplayName("Should fail validation when customer email has invalid format - no @ symbol")
        void shouldFail_WhenEmailHasNoAtSymbol() {
            // Arrange
            Order order = createValidOrder();
            order.setCustomerEmail("customerexample.com");

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Invalid email format");
        }

        @Test
        @DisplayName("Should fail validation when customer email has invalid format - no domain")
        void shouldFail_WhenEmailHasNoDomain() {
            // Arrange
            Order order = createValidOrder();
            order.setCustomerEmail("customer@");

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Invalid email format");
        }

        @Test
        @DisplayName("Should fail validation when customer email has invalid format - no TLD")
        void shouldFail_WhenEmailHasNoTLD() {
            // Arrange
            Order order = createValidOrder();
            order.setCustomerEmail("customer@example");

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Invalid email format");
        }
    }

    @Nested
    @DisplayName("Order Date Validation")
    class OrderDateValidation {

        @Test
        @DisplayName("Should pass validation when order date is today")
        void shouldPass_WhenOrderDateIsToday() {
            // Arrange
            Order order = createValidOrder();
            order.setOrderDate(LocalDate.now());

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when order date is in the past")
        void shouldPass_WhenOrderDateIsInPast() {
            // Arrange
            Order order = createValidOrder();
            order.setOrderDate(LocalDate.now().minusDays(10));

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when order date is one day in the past")
        void shouldPass_WhenOrderDateIsYesterday() {
            // Arrange
            Order order = createValidOrder();
            order.setOrderDate(LocalDate.now().minusDays(1));

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when order date is one year in the past")
        void shouldPass_WhenOrderDateIsOneYearInPast() {
            // Arrange
            Order order = createValidOrder();
            order.setOrderDate(LocalDate.now().minusYears(1));

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation when order date is null")
        void shouldFail_WhenOrderDateIsNull() {
            // Arrange
            Order order = createValidOrder();
            order.setOrderDate(null);

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Order date is required");
        }

        @Test
        @DisplayName("Should fail validation when order date is in the future")
        void shouldFail_WhenOrderDateIsInFuture() {
            // Arrange
            Order order = createValidOrder();
            order.setOrderDate(LocalDate.now().plusDays(1));

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Order date cannot be in the future");
        }

        @Test
        @DisplayName("Should fail validation when order date is one week in the future")
        void shouldFail_WhenOrderDateIsOneWeekInFuture() {
            // Arrange
            Order order = createValidOrder();
            order.setOrderDate(LocalDate.now().plusWeeks(1));

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Order date cannot be in the future");
        }

        @Test
        @DisplayName("Should fail validation when order date is one year in the future")
        void shouldFail_WhenOrderDateIsOneYearInFuture() {
            // Arrange
            Order order = createValidOrder();
            order.setOrderDate(LocalDate.now().plusYears(1));

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Order date cannot be in the future");
        }
    }

    @Nested
    @DisplayName("Multiple Constraint Violations")
    class MultipleViolations {

        @Test
        @DisplayName("Should detect all violations when multiple fields are invalid")
        void shouldDetectAllViolations_WhenMultipleFieldsAreInvalid() {
            // Arrange
            Order order = new Order();
            order.setId(1L);
            order.setItems(new ArrayList<>()); // Empty items
            order.setTotalAmount(new BigDecimal("-10.00")); // Negative amount
            order.setCustomerEmail("invalid-email"); // Invalid email
            order.setOrderDate(LocalDate.now().plusDays(1)); // Future date

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).hasSize(5);

            List<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();

            assertThat(messages).contains(
                "Order items cannot be empty",
                "At least one order item is required",
                "Total amount must be positive",
                "Invalid email format",
                "Order date cannot be in the future"
            );
        }

        @Test
        @DisplayName("Should validate all properties even when all are null")
        void shouldValidateAllProperties_WhenAllAreNull() {
            // Arrange
            Order order = new Order();

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).hasSize(5);

            List<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();

            assertThat(messages).contains(
                "Order ID is required",
                "Order items cannot be empty",
                "Total amount is required",
                "Customer email is required",
                "Order date is required"
            );
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should pass validation with large positive total amount")
        void shouldPass_WithLargePositiveTotalAmount() {
            // Arrange
            Order order = createValidOrder();
            order.setTotalAmount(new BigDecimal("999999999.99"));

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with complex email format")
        void shouldPass_WithComplexEmailFormat() {
            // Arrange
            Order order = createValidOrder();
            order.setCustomerEmail("user.name+tag@subdomain.example.co.uk");

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with very old order date")
        void shouldPass_WithVeryOldOrderDate() {
            // Arrange
            Order order = createValidOrder();
            order.setOrderDate(LocalDate.of(2000, 1, 1));

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should validate order with multiple valid items")
        void shouldValidate_WithMultipleValidItems() {
            // Arrange
            Order order = createValidOrder();
            order.setItems(List.of(
                new OrderItem(1L, "Product 1", 2, new BigDecimal("10.00")),
                new OrderItem(2L, "Product 2", 1, new BigDecimal("25.50")),
                new OrderItem(3L, "Product 3", 5, new BigDecimal("3.00"))
            ));
            order.setTotalAmount(new BigDecimal("60.50"));

            // Act
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            // Assert
            assertThat(violations).isEmpty();
        }
    }
}
