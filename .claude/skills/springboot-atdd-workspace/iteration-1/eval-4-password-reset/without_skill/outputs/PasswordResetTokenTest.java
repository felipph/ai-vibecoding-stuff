package com.example.auth.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PasswordResetToken domain model.
 */
@DisplayName("Password Reset Token")
class PasswordResetTokenTest {

    @Nested
    @DisplayName("Token Generation")
    class TokenGeneration {

        @Test
        @DisplayName("should generate unique tokens")
        void shouldGenerateUniqueTokens() {
            // Act
            String token1 = PasswordResetToken.generateToken();
            String token2 = PasswordResetToken.generateToken();

            // Assert
            assertThat(token1).isNotBlank();
            assertThat(token2).isNotBlank();
            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("should generate UUID format token")
        void shouldGenerateUuidFormatToken() {
            // Act
            String token = PasswordResetToken.generateToken();

            // Assert - UUID format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
            assertThat(token).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }
    }

    @Nested
    @DisplayName("Token Expiration")
    class TokenExpiration {

        @Test
        @DisplayName("should not be expired when expiration is in the future")
        void shouldNotBeExpiredWhenExpirationIsInTheFuture() {
            // Arrange
            Instant futureExpiry = Instant.now().plus(1, ChronoUnit.HOURS);
            PasswordResetToken token = new PasswordResetToken(1L, "token", futureExpiry);

            // Act
            boolean isExpired = token.isExpired();

            // Assert
            assertThat(isExpired).isFalse();
        }

        @Test
        @DisplayName("should be expired when expiration is in the past")
        void shouldBeExpiredWhenExpirationIsInThePast() {
            // Arrange
            Instant pastExpiry = Instant.now().minus(1, ChronoUnit.HOURS);
            PasswordResetToken token = new PasswordResetToken(1L, "token", pastExpiry);

            // Act
            boolean isExpired = token.isExpired();

            // Assert
            assertThat(isExpired).isTrue();
        }

        @Test
        @DisplayName("should be expired when expiration is exactly now")
        void shouldBeExpiredWhenExpirationIsExactlyNow() {
            // Arrange
            Instant nowExpiry = Instant.now();
            PasswordResetToken token = new PasswordResetToken(1L, "token", nowExpiry);

            // Act
            boolean isExpired = token.isExpired();

            // Assert
            assertThat(isExpired).isTrue();
        }
    }

    @Nested
    @DisplayName("Token Validity")
    class TokenValidity {

        @Test
        @DisplayName("should be valid when not expired and not used")
        void shouldBeValidWhenNotExpiredAndNotUsed() {
            // Arrange
            Instant futureExpiry = Instant.now().plus(1, ChronoUnit.HOURS);
            PasswordResetToken token = new PasswordResetToken(1L, "token", futureExpiry, false);

            // Act
            boolean isValid = token.isValid();

            // Assert
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("should be invalid when expired")
        void shouldBeInvalidWhenExpired() {
            // Arrange
            Instant pastExpiry = Instant.now().minus(1, ChronoUnit.HOURS);
            PasswordResetToken token = new PasswordResetToken(1L, "token", pastExpiry, false);

            // Act
            boolean isValid = token.isValid();

            // Assert
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("should be invalid when used")
        void shouldBeInvalidWhenUsed() {
            // Arrange
            Instant futureExpiry = Instant.now().plus(1, ChronoUnit.HOURS);
            PasswordResetToken token = new PasswordResetToken(1L, "token", futureExpiry, true);

            // Act
            boolean isValid = token.isValid();

            // Assert
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("should be invalid when both expired and used")
        void shouldBeInvalidWhenBothExpiredAndUsed() {
            // Arrange
            Instant pastExpiry = Instant.now().minus(1, ChronoUnit.HOURS);
            PasswordResetToken token = new PasswordResetToken(1L, "token", pastExpiry, true);

            // Act
            boolean isValid = token.isValid();

            // Assert
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("Token Properties")
    class TokenProperties {

        @Test
        @DisplayName("should store user id correctly")
        void shouldStoreUserIdCorrectly() {
            // Arrange
            Long userId = 42L;
            PasswordResetToken token = new PasswordResetToken(userId, "token", Instant.now());

            // Act & Assert
            assertThat(token.getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("should store token value correctly")
        void shouldStoreTokenValueCorrectly() {
            // Arrange
            String tokenValue = "unique-token-value";
            PasswordResetToken token = new PasswordResetToken(1L, tokenValue, Instant.now());

            // Act & Assert
            assertThat(token.getToken()).isEqualTo(tokenValue);
        }

        @Test
        @DisplayName("should store expiration correctly")
        void shouldStoreExpirationCorrectly() {
            // Arrange
            Instant expiresAt = Instant.now().plus(2, ChronoUnit.HOURS);
            PasswordResetToken token = new PasswordResetToken(1L, "token", expiresAt);

            // Act & Assert
            assertThat(token.getExpiresAt()).isEqualTo(expiresAt);
        }

        @Test
        @DisplayName("should have used status false by default")
        void shouldHaveUsedStatusFalseByDefault() {
            // Arrange
            PasswordResetToken token = new PasswordResetToken(1L, "token", Instant.now());

            // Act & Assert
            assertThat(token.isUsed()).isFalse();
        }

        @Test
        @DisplayName("should store used status when provided")
        void shouldStoreUsedStatusWhenProvided() {
            // Arrange
            PasswordResetToken token = new PasswordResetToken(1L, "token", Instant.now(), true);

            // Act & Assert
            assertThat(token.isUsed()).isTrue();
        }
    }
}
