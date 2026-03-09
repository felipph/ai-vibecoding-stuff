package com.example.auth.domain.exception;

/**
 * Exception thrown when a password reset token has already been used.
 */
public class TokenUsedException extends RuntimeException {

    public TokenUsedException(String message) {
        super(message);
    }

    public TokenUsedException(String message, Throwable cause) {
        super(message, cause);
    }
}
