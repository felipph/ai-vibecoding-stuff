package com.example.product;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

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
     * Updates the price of a product.
     *
     * @param id the product ID
     * @param request the price update request
     * @return the updated product
     */
    @PatchMapping("/{id}/price")
    public ResponseEntity<Product> updatePrice(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePriceRequest request) {

        Product updatedProduct = productService.updatePrice(id, request.getPrice());
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Handles ProductNotFoundException and returns 404 with error details.
     *
     * @param ex the exception
     * @return error response with 404 status
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("Product not found", ex.getProductId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
