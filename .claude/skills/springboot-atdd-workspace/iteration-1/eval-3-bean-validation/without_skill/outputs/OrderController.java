package com.example.order.controller;

import com.example.order.entity.Order;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for order operations with validation.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    /**
     * Creates a new order.
     * Spring automatically validates the order based on Bean Validation annotations.
     *
     * @param order the order to create
     * @return the created order with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody Order order) {
        // In a real application, this would call a service layer
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Updates an existing order.
     * Spring automatically validates the order based on Bean Validation annotations.
     *
     * @param id the order ID
     * @param order the order data to update
     * @return the updated order with HTTP 200 status
     */
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody Order order) {
        // In a real application, this would call a service layer
        order.setId(id);
        return ResponseEntity.ok(order);
    }
}
