package com.example.order.entity;

import jakarta.validation.Valid;
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

    @NotNull(message = "Order ID is required")
    private Long id;

    @NotEmpty(message = "Order items cannot be empty")
    @Size(min = 1, message = "At least one order item is required")
    @Valid
    private List<OrderItem> items = new ArrayList<>();

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private BigDecimal totalAmount;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotNull(message = "Order date is required")
    @PastOrPresent(message = "Order date cannot be in the future")
    private LocalDate orderDate;

    // Constructors
    public Order() {
    }

    public Order(Long id, List<OrderItem> items, BigDecimal totalAmount,
                 String customerEmail, LocalDate orderDate) {
        this.id = id;
        this.items = items;
        this.totalAmount = totalAmount;
        this.customerEmail = customerEmail;
        this.orderDate = orderDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    // Helper method to add items
    public void addItem(OrderItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }
}
