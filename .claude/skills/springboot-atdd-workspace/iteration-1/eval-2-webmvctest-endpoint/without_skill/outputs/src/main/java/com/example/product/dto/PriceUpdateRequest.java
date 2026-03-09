package com.example.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for price update requests.
 */
public class PriceUpdateRequest {

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    // Default constructor
    public PriceUpdateRequest() {}

    // Constructor with price
    public PriceUpdateRequest(BigDecimal price) {
        this.price = price;
    }

    // Getters and Setters
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
