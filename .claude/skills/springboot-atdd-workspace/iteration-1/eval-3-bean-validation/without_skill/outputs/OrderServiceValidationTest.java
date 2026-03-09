package com.example.order.service;

import com.example.order.entity.Order;
import com.example.order.entity.OrderItem;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for Order validation at the service layer.
 *
 * These tests verify that Spring's method-level validation works correctly
 * when using @Valid annotations on service methods.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Order Service Validation Tests")
class OrderServiceValidationTest {

    @Autowired
    private OrderService orderService;

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
    @DisplayName("Create Order Validation")
    class CreateOrderValidation {

        @Test
        @DisplayName("Should create order successfully when all validations pass")
        void shouldCreateOrder_WhenValidationsPass() {
            // Arrange
            Order order = createValidOrder();

            // Act
            Order created = orderService.createOrder(order);

            // Assert
            assertThat(created).isNotNull();
            assertThat(created.getId()).isEqualTo(order.getId());
        }

        @Test
        @DisplayName("Should throw exception when creating order with empty items")
        void shouldThrowException_WhenCreatingOrderWithEmptyItems() {
            // Arrange
            Order order = createValidOrder();
            order.setItems(List.of());

            // Act & Assert
            assertThatThrownBy(() -> orderService.createOrder(order))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(ex -> {
                    ConstraintViolationException cve = (ConstraintViolationException) ex;
                    assertThat(cve.getConstraintViolations())
                        .extracting(ConstraintViolation::getMessage)
                        .containsAnyOf(
                            "Order items cannot be empty",
                            "At least one order item is required"
                        );
                });
        }

        @Test
        @DisplayName("Should throw exception when creating order with negative total amount")
        void shouldThrowException_WhenCreatingOrderWithNegativeAmount() {
            // Arrange
            Order order = createValidOrder();
            order.setTotalAmount(new BigDecimal("-10.00"));

            // Act & Assert
            assertThatThrownBy(() -> orderService.createOrder(order))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(ex -> {
                    ConstraintViolationException cve = (ConstraintViolationException) ex;
                    assertThat(cve.getConstraintViolations())
                        .extracting(ConstraintViolation::getMessage)
                        .contains("Total amount must be positive");
                });
        }

        @Test
        @DisplayName("Should throw exception when creating order with invalid email")
        void shouldThrowException_WhenCreatingOrderWithInvalidEmail() {
            // Arrange
            Order order = createValidOrder();
            order.setCustomerEmail("invalid-email");

            // Act & Assert
            assertThatThrownBy(() -> orderService.createOrder(order))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(ex -> {
                    ConstraintViolationException cve = (ConstraintViolationException) ex;
                    assertThat(cve.getConstraintViolations())
                        .extracting(ConstraintViolation::getMessage)
                        .contains("Invalid email format");
                });
        }

        @Test
        @DisplayName("Should throw exception when creating order with future date")
        void shouldThrowException_WhenCreatingOrderWithFutureDate() {
            // Arrange
            Order order = createValidOrder();
            order.setOrderDate(LocalDate.now().plusDays(1));

            // Act & Assert
            assertThatThrownBy(() -> orderService.createOrder(order))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(ex -> {
                    ConstraintViolationException cve = (ConstraintViolationException) ex;
                    assertThat(cve.getConstraintViolations())
                        .extracting(ConstraintViolation::getMessage)
                        .contains("Order date cannot be in the future");
                });
        }

        @Test
        @DisplayName("Should throw exception when order is null")
        void shouldThrowException_WhenOrderIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> orderService.createOrder(null))
                .isInstanceOf(ConstraintViolationException.class);
        }
    }

    @Nested
    @DisplayName("Update Order Validation")
    class UpdateOrderValidation {

        @Test
        @DisplayName("Should update order successfully when all validations pass")
        void shouldUpdateOrder_WhenValidationsPass() {
            // Arrange
            Order order = createValidOrder();

            // Act
            Order updated = orderService.updateOrder(order);

            // Assert
            assertThat(updated).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when updating order with empty items")
        void shouldThrowException_WhenUpdatingOrderWithEmptyItems() {
            // Arrange
            Order order = createValidOrder();
            order.setItems(List.of());

            // Act & Assert
            assertThatThrownBy(() -> orderService.updateOrder(order))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(ex -> {
                    ConstraintViolationException cve = (ConstraintViolationException) ex;
                    assertThat(cve.getConstraintViolations())
                        .extracting(ConstraintViolation::getMessage)
                        .containsAnyOf(
                            "Order items cannot be empty",
                            "At least one order item is required"
                        );
                });
        }

        @Test
        @DisplayName("Should throw exception when updating order with zero total amount")
        void shouldThrowException_WhenUpdatingOrderWithZeroAmount() {
            // Arrange
            Order order = createValidOrder();
            order.setTotalAmount(BigDecimal.ZERO);

            // Act & Assert
            assertThatThrownBy(() -> orderService.updateOrder(order))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(ex -> {
                    ConstraintViolationException cve = (ConstraintViolationException) ex;
                    assertThat(cve.getConstraintViolations())
                        .extracting(ConstraintViolation::getMessage)
                        .contains("Total amount must be positive");
                });
        }
    }
}
