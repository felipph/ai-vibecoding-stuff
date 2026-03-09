package com.example.auth.domain.service;

import com.example.auth.domain.exception.InvalidTokenException;
import com.example.auth.domain.exception.TokenExpiredException;
import com.example.auth.domain.exception.TokenUsedException;
import com.example.auth.domain.model.PasswordResetToken;
import com.example.auth.domain.model.User;
import com.example.auth.domain.repository.PasswordResetTokenRepository;
import com.example.auth.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Domain service for password reset operations.
 * Handles token creation, validation, and password reset.
 */
@Service
@Transactional
public class PasswordResetService {

    private static final int TOKEN_VALIDITY_HOURS = 1;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    /**
     * Requests a password reset for the given email.
     * If the email is registered, a reset token is created and sent.
     * If the email is not registered, no action is taken (but returns success for security).
     *
     * @param email the email address to request reset for
     */
    public void requestPasswordReset(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        // For security, always return success even if email doesn't exist
        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();

        // Invalidate any previous tokens for this user
        tokenRepository.invalidateAllTokensForUser(user.getId());

        // Create new token valid for 1 hour
        String tokenValue = PasswordResetToken.generateToken();
        Instant expiresAt = Instant.now().plus(TOKEN_VALIDITY_HOURS, ChronoUnit.HOURS);

        PasswordResetToken token = new PasswordResetToken(user.getId(), tokenValue, expiresAt);
        tokenRepository.save(token);

        // Send reset email
        emailService.sendPasswordResetEmail(email, tokenValue);
    }

    /**
     * Resets the password using the provided token.
     *
     * @param tokenValue the reset token
     * @param newPassword the new password
     * @throws InvalidTokenException if the token is not found
     * @throws TokenExpiredException if the token has expired
     * @throws TokenUsedException if the token has already been used
     */
    public void resetPassword(String tokenValue, String newPassword) {
        PasswordResetToken token = tokenRepository.findByToken(tokenValue)
            .orElseThrow(() -> new InvalidTokenException("Invalid token: token not found"));

        if (token.isExpired()) {
            throw new TokenExpiredException("Token has expired. Please request a new password reset.");
        }

        if (token.isUsed()) {
            throw new TokenUsedException("Token has already been used. Please request a new password reset.");
        }

        // Update password
        userRepository.updatePassword(token.getUserId(), newPassword);

        // Mark token as used
        tokenRepository.markAsUsed(tokenValue);

        // Send confirmation email
        userRepository.findById(token.getUserId())
            .ifPresent(user -> emailService.sendPasswordChangedConfirmation(user.getEmail()));
    }

    /**
     * Validates if a token is valid (exists, not expired, not used).
     *
     * @param tokenValue the token to validate
     * @return true if valid, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isTokenValid(String tokenValue) {
        return tokenRepository.findByToken(tokenValue)
            .map(PasswordResetToken::isValid)
            .orElse(false);
    }
}
