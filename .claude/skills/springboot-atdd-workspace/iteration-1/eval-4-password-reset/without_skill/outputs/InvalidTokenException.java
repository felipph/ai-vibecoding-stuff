package com.example.auth.domain.exception;

/**
 * Exception thrown when a password reset token is invalid or not found.
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
