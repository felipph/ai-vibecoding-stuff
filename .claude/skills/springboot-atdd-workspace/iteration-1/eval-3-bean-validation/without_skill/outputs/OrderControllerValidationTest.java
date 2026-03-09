package com.example.order.controller;

import com.example.order.entity.Order;
import com.example.order.entity.OrderItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for Order validation at the REST controller layer.
 *
 * These tests verify that Spring MVC automatically validates request bodies
 * and returns appropriate error responses when validation fails.
 */
@WebMvcTest(OrderController.class)
@DisplayName("Order Controller Validation Tests")
class OrderControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("POST /api/orders - Create Order")
    class CreateOrderValidation {

        @Test
        @DisplayName("Should return 201 when order is valid")
        void shouldReturn201_WhenOrderIsValid() throws Exception {
            // Arrange
            Order order = createValidOrder();

            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.customerEmail").value(order.getCustomerEmail()));
        }

        @Test
        @DisplayName("Should return 400 when order items list is empty")
        void shouldReturn400_WhenOrderItemsIsEmpty() throws Exception {
            // Arrange
            Order order = createValidOrder();
            order.setItems(new ArrayList<>());

            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field == 'items')]").exists());
        }

        @Test
        @DisplayName("Should return 400 when order items list is null")
        void shouldReturn400_WhenOrderItemsIsNull() throws Exception {
            // Arrange
            Order order = createValidOrder();
            order.setItems(null);

            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field == 'items')]").exists());
        }

        @Test
        @DisplayName("Should return 400 when total amount is negative")
        void shouldReturn400_WhenTotalAmountIsNegative() throws Exception {
            // Arrange
            Order order = createValidOrder();
            order.setTotalAmount(new BigDecimal("-10.00"));

            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field == 'totalAmount')]").exists());
        }

        @Test
        @DisplayName("Should return 400 when total amount is zero")
        void shouldReturn400_WhenTotalAmountIsZero() throws Exception {
            // Arrange
            Order order = createValidOrder();
            order.setTotalAmount(BigDecimal.ZERO);

            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field == 'totalAmount')]").exists());
        }

        @Test
        @DisplayName("Should return 400 when customer email is missing")
        void shouldReturn400_WhenCustomerEmailIsMissing() throws Exception {
            // Arrange
            Order order = createValidOrder();
            order.setCustomerEmail(null);

            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field == 'customerEmail')]").exists());
        }

        @Test
        @DisplayName("Should return 400 when customer email is empty")
        void shouldReturn400_WhenCustomerEmailIsEmpty() throws Exception {
            // Arrange
            Order order = createValidOrder();
            order.setCustomerEmail("");

            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field == 'customerEmail')]").exists());
        }

        @Test
        @DisplayName("Should return 400 when customer email format is invalid")
        void shouldReturn400_WhenCustomerEmailFormatIsInvalid() throws Exception {
            // Arrange
            Order order = createValidOrder();
            order.setCustomerEmail("invalid-email");

            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field == 'customerEmail')]").exists());
        }

        @Test
        @DisplayName("Should return 400 when order date is in the future")
        void shouldReturn400_WhenOrderDateIsInFuture() throws Exception {
            // Arrange
            Order order = createValidOrder();
            order.setOrderDate(LocalDate.now().plusDays(1));

            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field == 'orderDate')]").exists());
        }

        @Test
        @DisplayName("Should return 400 with multiple errors when multiple fields are invalid")
        void shouldReturn400_WithMultipleErrors_WhenMultipleFieldsAreInvalid() throws Exception {
            // Arrange
            Order order = new Order();
            order.setId(1L);
            order.setItems(new ArrayList<>());
            order.setTotalAmount(new BigDecimal("-10.00"));
            order.setCustomerEmail("invalid-email");
            order.setOrderDate(LocalDate.now().plusDays(1));

            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(org.hamcrest.Matchers.greaterThan(1)));
        }

        @Test
        @DisplayName("Should return 201 when order date is in the past")
        void shouldReturn201_WhenOrderDateIsInPast() throws Exception {
            // Arrange
            Order order = createValidOrder();
            order.setOrderDate(LocalDate.now().minusDays(5));

            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("PUT /api/orders/{id} - Update Order")
    class UpdateOrderValidation {

        @Test
        @DisplayName("Should return 200 when order is valid")
        void shouldReturn200_WhenOrderIsValid() throws Exception {
            // Arrange
            Order order = createValidOrder();
            Long orderId = 1L;

            // Act & Assert
            mockMvc.perform(put("/api/orders/{id}", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));
        }

        @Test
        @DisplayName("Should return 400 when updating order with empty items")
        void shouldReturn400_WhenUpdatingOrderWithEmptyItems() throws Exception {
            // Arrange
            Order order = createValidOrder();
            order.setItems(new ArrayList<>());
            Long orderId = 1L;

            // Act & Assert
            mockMvc.perform(put("/api/orders/{id}", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
        }

        @Test
        @DisplayName("Should return 400 when updating order with negative amount")
        void shouldReturn400_WhenUpdatingOrderWithNegativeAmount() throws Exception {
            // Arrange
            Order order = createValidOrder();
            order.setTotalAmount(new BigDecimal("-50.00"));
            Long orderId = 1L;

            // Act & Assert
            mockMvc.perform(put("/api/orders/{id}", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
        }

        @Test
        @DisplayName("Should return 400 when updating order with invalid email")
        void shouldReturn400_WhenUpdatingOrderWithInvalidEmail() throws Exception {
            // Arrange
            Order order = createValidOrder();
            order.setCustomerEmail("not-an-email");
            Long orderId = 1L;

            // Act & Assert
            mockMvc.perform(put("/api/orders/{id}", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
        }

        @Test
        @DisplayName("Should return 400 when updating order with future date")
        void shouldReturn400_WhenUpdatingOrderWithFutureDate() throws Exception {
            // Arrange
            Order order = createValidOrder();
            order.setOrderDate(LocalDate.now().plusMonths(1));
            Long orderId = 1L;

            // Act & Assert
            mockMvc.perform(put("/api/orders/{id}", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
        }
    }

    @Nested
    @DisplayName("Cascade Validation Tests")
    class CascadeValidationTests {

        @Test
        @DisplayName("Should return 400 when nested order item is invalid")
        void shouldReturn400_WhenNestedOrderItemIsInvalid() throws Exception {
            // Arrange
            Order order = createValidOrder();
            order.setItems(List.of(
                new OrderItem(null, "", -1, new BigDecimal("-5.00"))
            ));

            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(org.hamcrest.Matchers.greaterThan(1)));
        }

        @Test
        @DisplayName("Should return 201 when all nested order items are valid")
        void shouldReturn201_WhenAllNestedOrderItemsAreValid() throws Exception {
            // Arrange
            Order order = createValidOrder();
            order.setItems(List.of(
                new OrderItem(1L, "Product 1", 2, new BigDecimal("10.00")),
                new OrderItem(2L, "Product 2", 1, new BigDecimal("25.00")),
                new OrderItem(3L, "Product 3", 3, new BigDecimal("15.00"))
            ));
            order.setTotalAmount(new BigDecimal("90.00"));

            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items.length()").value(3));
        }
    }
}
