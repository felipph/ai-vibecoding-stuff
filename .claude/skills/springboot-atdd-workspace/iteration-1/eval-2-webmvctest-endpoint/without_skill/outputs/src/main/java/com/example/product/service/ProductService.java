package com.example.product.service;

import com.example.product.dto.PriceUpdateRequest;
import com.example.product.exception.InvalidPriceException;
import com.example.product.exception.ProductNotFoundException;
import com.example.product.model.Product;
import com.example.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service for managing products.
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Update the price of a product.
     *
     * @param productId the product ID
     * @param priceUpdateRequest the price update request
     * @return the updated product
     * @throws ProductNotFoundException if product doesn't exist
     * @throws InvalidPriceException if price is not positive
     */
    @Transactional
    public Product updatePrice(Long productId, PriceUpdateRequest priceUpdateRequest) {
        // Validate price is positive (additional validation at service layer)
        BigDecimal newPrice = priceUpdateRequest.getPrice();
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPriceException("Price must be positive");
        }

        // Find the product
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        // Update the price
        product.setPrice(newPrice);

        // Save and return
        return productRepository.save(product);
    }

    /**
     * Find a product by ID.
     *
     * @param productId the product ID
     * @return the product
     * @throws ProductNotFoundException if product doesn't exist
     */
    public Product findById(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
