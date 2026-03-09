package com.example.auth.domain;

import com.example.auth.domain.model.PasswordResetToken;
import com.example.auth.domain.model.User;
import com.example.auth.domain.repository.PasswordResetTokenRepository;
import com.example.auth.domain.repository.UserRepository;
import com.example.auth.domain.service.PasswordResetService;
import com.example.auth.domain.exception.InvalidTokenException;
import com.example.auth.domain.exception.TokenExpiredException;
import com.example.auth.domain.exception.TokenUsedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PasswordResetService using TDD approach.
 * Tests cover all acceptance criteria for password reset functionality.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Password Reset Service")
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(userRepository, tokenRepository, emailService);
    }

    @Nested
    @DisplayName("Request Password Reset")
    class RequestPasswordReset {

        @Test
        @DisplayName("should create and send reset token for registered email")
        void shouldCreateAndSendResetTokenForRegisteredEmail() {
            // Arrange
            String email = "user@example.com";
            User user = new User(1L, email, "hashedPassword");

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            passwordResetService.requestPasswordReset(email);

            // Assert
            ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(tokenRepository).save(tokenCaptor.capture());

            PasswordResetToken savedToken = tokenCaptor.getValue();
            assertThat(savedToken.getUserId()).isEqualTo(1L);
            assertThat(savedToken.getToken()).isNotBlank();
            assertThat(savedToken.getExpiresAt()).isAfter(Instant.now());
            assertThat(savedToken.isUsed()).isFalse();

            verify(emailService).sendPasswordResetEmail(eq(email), any(String.class));
        }

        @Test
        @DisplayName("should show generic success message for unregistered email")
        void shouldShowGenericSuccessMessageForUnregisteredEmail() {
            // Arrange
            String email = "nonexistent@example.com";
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            // Act
            passwordResetService.requestPasswordReset(email);

            // Assert - should NOT throw exception, should not send email
            verify(tokenRepository, never()).save(any());
            verify(emailService, never()).sendPasswordResetEmail(any(), any());
        }

        @Test
        @DisplayName("should invalidate previous tokens when requesting new reset")
        void shouldInvalidatePreviousTokensWhenRequestingNewReset() {
            // Arrange
            String email = "user@example.com";
            User user = new User(1L, email, "hashedPassword");

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            passwordResetService.requestPasswordReset(email);

            // Assert
            verify(tokenRepository).invalidateAllTokensForUser(1L);
        }

        @Test
        @DisplayName("should create token valid for exactly 1 hour")
        void shouldCreateTokenValidForExactlyOneHour() {
            // Arrange
            String email = "user@example.com";
            User user = new User(1L, email, "hashedPassword");
            Instant beforeCreation = Instant.now();

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            passwordResetService.requestPasswordReset(email);

            // Assert
            ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(tokenRepository).save(tokenCaptor.capture());

            PasswordResetToken savedToken = tokenCaptor.getValue();
            Instant expectedExpiry = beforeCreation.plus(1, ChronoUnit.HOURS);

            // Allow 5 second tolerance for test execution time
            assertThat(savedToken.getExpiresAt())
                .isBetween(expectedExpiry.minus(5, ChronoUnit.SECONDS),
                          expectedExpiry.plus(5, ChronoUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("Reset Password with Token")
    class ResetPasswordWithToken {

        @Test
        @DisplayName("should reset password with valid token")
        void shouldResetPasswordWithValidToken() {
            // Arrange
            String tokenValue = "valid-token-123";
            String newPassword = "newSecurePassword123";
            Long userId = 1L;

            PasswordResetToken token = new PasswordResetToken(
                userId,
                tokenValue,
                Instant.now().plus(1, ChronoUnit.HOURS)
            );

            when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));
            when(userRepository.findById(userId)).thenReturn(Optional.of(new User(userId, "user@example.com", "oldHash")));

            // Act
            passwordResetService.resetPassword(tokenValue, newPassword);

            // Assert
            verify(userRepository).updatePassword(userId, newPassword);
            verify(tokenRepository).markAsUsed(tokenValue);
        }

        @Test
        @DisplayName("should reject expired token")
        void shouldRejectExpiredToken() {
            // Arrange
            String tokenValue = "expired-token-123";
            PasswordResetToken expiredToken = new PasswordResetToken(
                1L,
                tokenValue,
                Instant.now().minus(1, ChronoUnit.HOURS) // Expired 1 hour ago
            );

            when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(expiredToken));

            // Act & Assert
            assertThatThrownBy(() -> passwordResetService.resetPassword(tokenValue, "newPassword123"))
                .isInstanceOf(TokenExpiredException.class)
                .hasMessageContaining("Token has expired");

            verify(userRepository, never()).updatePassword(any(), any());
            verify(tokenRepository, never()).markAsUsed(any());
        }

        @Test
        @DisplayName("should reject already used token")
        void shouldRejectAlreadyUsedToken() {
            // Arrange
            String tokenValue = "used-token-123";
            PasswordResetToken usedToken = new PasswordResetToken(
                1L,
                tokenValue,
                Instant.now().plus(1, ChronoUnit.HOURS),
                true // Already used
            );

            when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(usedToken));

            // Act & Assert
            assertThatThrownBy(() -> passwordResetService.resetPassword(tokenValue, "newPassword123"))
                .isInstanceOf(TokenUsedException.class)
                .hasMessageContaining("Token has already been used");

            verify(userRepository, never()).updatePassword(any(), any());
        }

        @Test
        @DisplayName("should reject invalid token")
        void shouldRejectInvalidToken() {
            // Arrange
            String tokenValue = "invalid-token-123";
            when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> passwordResetService.resetPassword(tokenValue, "newPassword123"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid token");

            verify(userRepository, never()).updatePassword(any(), any());
            verify(tokenRepository, never()).markAsUsed(any());
        }

        @Test
        @DisplayName("should mark token as used after successful password reset")
        void shouldMarkTokenAsUsedAfterSuccessfulPasswordReset() {
            // Arrange
            String tokenValue = "valid-token-123";
            PasswordResetToken token = new PasswordResetToken(
                1L,
                tokenValue,
                Instant.now().plus(1, ChronoUnit.HOURS)
            );

            when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));
            when(userRepository.findById(1L)).thenReturn(Optional.of(new User(1L, "user@example.com", "oldHash")));

            // Act
            passwordResetService.resetPassword(tokenValue, "newPassword123");

            // Assert
            verify(tokenRepository).markAsUsed(tokenValue);
        }
    }

    @Nested
    @DisplayName("Token Validation")
    class TokenValidation {

        @Test
        @DisplayName("should validate token exists and is not expired")
        void shouldValidateTokenExistsAndIsNotExpired() {
            // Arrange
            String tokenValue = "valid-token-123";
            PasswordResetToken token = new PasswordResetToken(
                1L,
                tokenValue,
                Instant.now().plus(1, ChronoUnit.HOURS)
            );

            when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

            // Act
            boolean isValid = passwordResetService.isTokenValid(tokenValue);

            // Assert
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("should return false for expired token")
        void shouldReturnFalseForExpiredToken() {
            // Arrange
            String tokenValue = "expired-token-123";
            PasswordResetToken expiredToken = new PasswordResetToken(
                1L,
                tokenValue,
                Instant.now().minus(1, ChronoUnit.HOURS)
            );

            when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(expiredToken));

            // Act
            boolean isValid = passwordResetService.isTokenValid(tokenValue);

            // Assert
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("should return false for used token")
        void shouldReturnFalseForUsedToken() {
            // Arrange
            String tokenValue = "used-token-123";
            PasswordResetToken usedToken = new PasswordResetToken(
                1L,
                tokenValue,
                Instant.now().plus(1, ChronoUnit.HOURS),
                true
            );

            when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(usedToken));

            // Act
            boolean isValid = passwordResetService.isTokenValid(tokenValue);

            // Assert
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("should return false for non-existent token")
        void shouldReturnFalseForNonExistentToken() {
            // Arrange
            String tokenValue = "nonexistent-token-123";
            when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.empty());

            // Act
            boolean isValid = passwordResetService.isTokenValid(tokenValue);

            // Assert
            assertThat(isValid).isFalse();
        }
    }
}
