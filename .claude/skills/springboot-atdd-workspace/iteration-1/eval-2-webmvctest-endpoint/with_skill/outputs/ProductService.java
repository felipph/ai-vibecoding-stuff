package com.example.product;

import java.math.BigDecimal;

/**
 * Service interface for product operations.
 */
public interface ProductService {

    /**
     * Updates the price of a product.
     *
     * @param productId the ID of the product to update
     * @param newPrice the new price (must be positive)
     * @return the updated product
     * @throws ProductNotFoundException if the product does not exist
     */
    Product updatePrice(Long productId, BigDecimal newPrice);
}
