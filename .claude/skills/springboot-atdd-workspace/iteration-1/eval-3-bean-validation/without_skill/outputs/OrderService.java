package com.example.order.service;

import com.example.order.entity.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * Service interface for order operations with validation.
 */
@Validated
public interface OrderService {

    /**
     * Creates a new order after validation.
     *
     * @param order the order to create
     * @return the created order
     * @throws jakarta.validation.ConstraintViolationException if validation fails
     */
    Order createOrder(@Valid @NotNull Order order);

    /**
     * Updates an existing order after validation.
     *
     * @param order the order to update
     * @return the updated order
     * @throws jakarta.validation.ConstraintViolationException if validation fails
     */
    Order updateOrder(@Valid @NotNull Order order);
}
