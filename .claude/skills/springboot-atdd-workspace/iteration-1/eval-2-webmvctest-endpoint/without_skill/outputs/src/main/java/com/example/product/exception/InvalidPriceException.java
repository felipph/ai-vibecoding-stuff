package com.example.product.exception;

/**
 * Exception thrown when a price is invalid.
 */
public class InvalidPriceException extends RuntimeException {

    public InvalidPriceException(String message) {
        super(message);
    }
}
