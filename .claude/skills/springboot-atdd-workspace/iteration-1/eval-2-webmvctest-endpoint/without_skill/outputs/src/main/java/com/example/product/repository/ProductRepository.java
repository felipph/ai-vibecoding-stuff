package com.example.product.repository;

import com.example.product.model.Product;
import java.util.Optional;

/**
 * Repository interface for Product operations.
 */
public interface ProductRepository {

    /**
     * Find a product by its ID.
     *
     * @param id the product ID
     * @return Optional containing the product if found
     */
    Optional<Product> findById(Long id);

    /**
     * Save a product.
     *
     * @param product the product to save
     * @return the saved product
     */
    Product save(Product product);

    /**
     * Check if a product exists by ID.
     *
     * @param id the product ID
     * @return true if exists, false otherwise
     */
    boolean existsById(Long id);
}
