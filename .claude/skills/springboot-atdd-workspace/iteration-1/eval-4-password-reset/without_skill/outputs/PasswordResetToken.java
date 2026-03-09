package com.example.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a password reset token.
 * Tokens are valid for 1 hour and can only be used once.
 */
public class PasswordResetToken {

    private final Long userId;
    private final String token;
    private final Instant expiresAt;
    private final boolean used;
    private final Instant createdAt;

    /**
     * Creates a new password reset token valid for 1 hour.
     *
     * @param userId the user ID associated with this token
     * @param token the token string (use generateToken() to create)
     * @param expiresAt the expiration timestamp
     */
    public PasswordResetToken(Long userId, String token, Instant expiresAt) {
        this(userId, token, expiresAt, false, Instant.now());
    }

    /**
     * Full constructor for reconstituting from persistence.
     */
    public PasswordResetToken(Long userId, String token, Instant expiresAt, boolean used) {
        this(userId, token, expiresAt, used, Instant.now());
    }

    /**
     * Complete constructor.
     */
    public PasswordResetToken(Long userId, String token, Instant expiresAt, boolean used, Instant createdAt) {
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.used = used;
        this.createdAt = createdAt;
    }

    /**
     * Generates a secure random token.
     *
     * @return a UUID-based token string
     */
    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Checks if this token has expired.
     *
     * @return true if the token has expired
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Checks if this token is valid (not expired and not used).
     *
     * @return true if the token can be used
     */
    public boolean isValid() {
        return !isExpired() && !used;
    }

    // Getters

    public Long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "PasswordResetToken{" +
            "userId=" + userId +
            ", token='" + token + '\'' +
            ", expiresAt=" + expiresAt +
            ", used=" + used +
            ", createdAt=" + createdAt +
            '}';
    }
}
