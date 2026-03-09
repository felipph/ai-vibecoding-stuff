package com.example.order;

import java.math.BigDecimal;

/**
 * Order item entity representing a product in an order.
 */
public class OrderItem {

    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;

    // Default constructor
    public OrderItem() {
    }

    // Constructor with all fields
    public OrderItem(String productName, Integer quantity, BigDecimal unitPrice) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    // Builder pattern for easier test construction
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;

        public Builder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public Builder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder unitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public Builder unitPrice(double unitPrice) {
            this.unitPrice = BigDecimal.valueOf(unitPrice);
            return this;
        }

        public OrderItem build() {
            return new OrderItem(productName, quantity, unitPrice);
        }
    }
}
