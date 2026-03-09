package com.example.auth.domain.repository;

import com.example.auth.domain.model.PasswordResetToken;

import java.util.Optional;

/**
 * Repository interface for PasswordResetToken persistence operations.
 */
public interface PasswordResetTokenRepository {

    /**
     * Finds a token by its value.
     *
     * @param token the token string
     * @return Optional containing the token if found
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Saves a new password reset token.
     *
     * @param token the token to save
     * @return the saved token
     */
    PasswordResetToken save(PasswordResetToken token);

    /**
     * Marks a token as used.
     *
     * @param token the token string to mark as used
     */
    void markAsUsed(String token);

    /**
     * Invalidates all tokens for a user (marks them as used).
     *
     * @param userId the user ID
     */
    void invalidateAllTokensForUser(Long userId);

    /**
     * Deletes expired tokens (cleanup operation).
     *
     * @return number of tokens deleted
     */
    int deleteExpiredTokens();
}
