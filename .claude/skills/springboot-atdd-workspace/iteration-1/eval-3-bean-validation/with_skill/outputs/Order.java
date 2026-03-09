package com.example.order;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity with Bean Validation constraints.
 *
 * Validation rules:
 * - Order items cannot be empty
 * - Total amount must be positive
 * - Customer email is required and valid
 * - Order date cannot be in the future
 */
public class Order {

    @NotNull(message = "Order items cannot be empty")
    @Size(min = 1, message = "Order items cannot be empty")
    private List<OrderItem> items;

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private BigDecimal totalAmount;

    @NotNull(message = "Customer email is required")
    @NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be valid")
    private String customerEmail;

    @NotNull(message = "Order date is required")
    @PastOrPresent(message = "Order date cannot be in the future")
    private LocalDate orderDate;

    // Default constructor
    public Order() {
        this.items = new ArrayList<>();
    }

    // Constructor with all fields
    public Order(List<OrderItem> items, BigDecimal totalAmount, String customerEmail, LocalDate orderDate) {
        this.items = items != null ? items : new ArrayList<>();
        this.totalAmount = totalAmount;
        this.customerEmail = customerEmail;
        this.orderDate = orderDate;
    }

    // Getters and Setters
    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    // Builder pattern for easier test construction
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<OrderItem> items = new ArrayList<>();
        private BigDecimal totalAmount;
        private String customerEmail;
        private LocalDate orderDate;

        public Builder items(List<OrderItem> items) {
            this.items = items;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder totalAmount(double totalAmount) {
            this.totalAmount = BigDecimal.valueOf(totalAmount);
            return this;
        }

        public Builder customerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
            return this;
        }

        public Builder orderDate(LocalDate orderDate) {
            this.orderDate = orderDate;
            return this;
        }

        public Order build() {
            return new Order(items, totalAmount, customerEmail, orderDate);
        }
    }
}
