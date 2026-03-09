package com.example.auth.service;

import com.example.auth.exception.InvalidTokenException;
import com.example.auth.model.PasswordResetToken;
import com.example.auth.model.User;
import com.example.auth.repository.PasswordResetTokenRepository;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for handling password reset functionality.
 *
 * Implements the following acceptance criteria:
 * [+] User can request password reset with registered email
 * [-] If email is not registered, show generic success message (security)
 * [+] System sends reset token valid for 1 hour
 * [+] User can reset password with valid token
 * [-] Expired tokens are rejected
 * [-] Used tokens cannot be reused
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final String GENERIC_SUCCESS_MESSAGE =
            "If your email is registered in our system, you will receive a password reset link.";
    private static final int TOKEN_VALIDITY_HOURS = 1;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private Clock clock = Clock.systemDefaultZone();

    /**
     * Sets the clock used for time-based operations (for testing).
     * @param clock the clock to use
     */
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    /**
     * Requests a password reset for the given email address.
     * Always returns a generic success message for security reasons.
     *
     * @param email the email address to reset password for
     * @return generic success message (same whether email exists or not)
     */
    @Transactional
    public String requestPasswordReset(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            // Invalidate any existing tokens for this email
            tokenRepository.deleteByEmail(email);

            // Create new token valid for 1 hour
            String token = generateToken();
            LocalDateTime now = LocalDateTime.now(clock);
            LocalDateTime expiryDate = now.plusHours(TOKEN_VALIDITY_HOURS);

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .email(email)
                    .expiryDate(expiryDate)
                    .used(false)
                    .build();

            tokenRepository.save(resetToken);

            // Send reset email
            emailService.sendPasswordResetEmail(email, token);
        }

        // Always return same message for security (don't reveal if email exists)
        return GENERIC_SUCCESS_MESSAGE;
    }

    /**
     * Resets a user's password using a valid reset token.
     *
     * @param token the password reset token
     * @param newPassword the new password
     * @throws InvalidTokenException if token is invalid, expired, or already used
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        // Find the token
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));

        LocalDateTime now = LocalDateTime.now(clock);

        // Check if token has expired
        if (resetToken.isExpired(now)) {
            throw new InvalidTokenException("Token has expired");
        }

        // Check if token has already been used
        if (resetToken.isUsed()) {
            throw new InvalidTokenException("Token has already been used");
        }

        // Find the user
        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new InvalidTokenException("User not found"));

        // Update password
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    /**
     * Generates a unique reset token.
     * @return a UUID-based token string
     */
    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}
