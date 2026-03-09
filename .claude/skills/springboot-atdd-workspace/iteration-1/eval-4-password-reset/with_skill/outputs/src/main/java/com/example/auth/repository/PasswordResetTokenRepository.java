package com.example.auth.repository;

import com.example.auth.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for PasswordResetToken entity.
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Finds a password reset token by its token string.
     * @param token the token string to search for
     * @return Optional containing the token if found
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Deletes all password reset tokens for a given email.
     * Used to invalidate previous tokens when a new one is requested.
     * @param email the email address
     */
    void deleteByEmail(String email);
}
