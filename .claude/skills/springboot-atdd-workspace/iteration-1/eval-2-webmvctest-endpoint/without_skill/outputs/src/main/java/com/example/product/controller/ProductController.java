package com.example.product.controller;

import com.example.product.dto.PriceUpdateRequest;
import com.example.product.model.Product;
import com.example.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing products.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Update the price of a product.
     *
     * @param productId the product ID
     * @param priceUpdateRequest the price update request
     * @return the updated product
     */
    @PatchMapping("/{productId}/price")
    public ResponseEntity<Product> updatePrice(
            @PathVariable Long productId,
            @Valid @RequestBody PriceUpdateRequest priceUpdateRequest) {

        Product updatedProduct = productService.updatePrice(productId, priceUpdateRequest);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Get a product by ID.
     *
     * @param productId the product ID
     * @return the product
     */
    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable Long productId) {
        Product product = productService.findById(productId);
        return ResponseEntity.ok(product);
    }
}
