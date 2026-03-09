package com.example.auth.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PasswordResetToken model.
 */
class PasswordResetTokenTest {

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2024, 3, 15, 10, 0, 0);

    @Nested
    @DisplayName("isExpired")
    class IsExpired {

        @Test
        @DisplayName("with expiry date in the future - returns false")
        void withExpiryDateInFuture_returnsFalse() {
            // Arrange
            PasswordResetToken token = PasswordResetToken.builder()
                    .token("abc123")
                    .email("user@example.com")
                    .expiryDate(FIXED_NOW.plusHours(1))
                    .used(false)
                    .build();

            // Act
            boolean expired = token.isExpired(FIXED_NOW);

            // Assert
            assertThat(expired).isFalse();
        }

        @Test
        @DisplayName("with expiry date exactly now - returns false (boundary)")
        void withExpiryDateExactlyNow_returnsFalse() {
            // Arrange
            PasswordResetToken token = PasswordResetToken.builder()
                    .token("abc123")
                    .email("user@example.com")
                    .expiryDate(FIXED_NOW)
                    .used(false)
                    .build();

            // Act
            boolean expired = token.isExpired(FIXED_NOW);

            // Assert - Token is expired if now IS AFTER expiry date, so equal is not expired
            assertThat(expired).isFalse();
        }

        @Test
        @DisplayName("with expiry date in the past - returns true")
        void withExpiryDateInPast_returnsTrue() {
            // Arrange
            PasswordResetToken token = PasswordResetToken.builder()
                    .token("abc123")
                    .email("user@example.com")
                    .expiryDate(FIXED_NOW.minusMinutes(1))
                    .used(false)
                    .build();

            // Act
            boolean expired = token.isExpired(FIXED_NOW);

            // Assert
            assertThat(expired).isTrue();
        }

        @Test
        @DisplayName("with expiry date 1 second in the past - returns true")
        void withExpiryDate1SecondInPast_returnsTrue() {
            // Arrange
            PasswordResetToken token = PasswordResetToken.builder()
                    .token("abc123")
                    .email("user@example.com")
                    .expiryDate(FIXED_NOW.minusSeconds(1))
                    .used(false)
                    .build();

            // Act
            boolean expired = token.isExpired(FIXED_NOW);

            // Assert
            assertThat(expired).isTrue();
        }

        @Test
        @DisplayName("with expiry date 1 second in the future - returns false")
        void withExpiryDate1SecondInFuture_returnsFalse() {
            // Arrange
            PasswordResetToken token = PasswordResetToken.builder()
                    .token("abc123")
                    .email("user@example.com")
                    .expiryDate(FIXED_NOW.plusSeconds(1))
                    .used(false)
                    .build();

            // Act
            boolean expired = token.isExpired(FIXED_NOW);

            // Assert
            assertThat(expired).isFalse();
        }
    }

    @Nested
    @DisplayName("isUsed")
    class IsUsed {

        @Test
        @DisplayName("with used = false - returns false")
        void withUsedFalse_returnsFalse() {
            // Arrange
            PasswordResetToken token = PasswordResetToken.builder()
                    .token("abc123")
                    .email("user@example.com")
                    .expiryDate(FIXED_NOW.plusHours(1))
                    .used(false)
                    .build();

            // Act & Assert
            assertThat(token.isUsed()).isFalse();
        }

        @Test
        @DisplayName("with used = true - returns true")
        void withUsedTrue_returnsTrue() {
            // Arrange
            PasswordResetToken token = PasswordResetToken.builder()
                    .token("abc123")
                    .email("user@example.com")
                    .expiryDate(FIXED_NOW.plusHours(1))
                    .used(true)
                    .build();

            // Act & Assert
            assertThat(token.isUsed()).isTrue();
        }
    }

    @Nested
    @DisplayName("builder")
    class BuilderTest {

        @Test
        @DisplayName("creates token with all fields")
        void createsTokenWithAllFields() {
            // Arrange & Act
            PasswordResetToken token = PasswordResetToken.builder()
                    .id(1L)
                    .token("abc123")
                    .email("user@example.com")
                    .expiryDate(FIXED_NOW.plusHours(1))
                    .used(false)
                    .build();

            // Assert
            assertThat(token.getId()).isEqualTo(1L);
            assertThat(token.getToken()).isEqualTo("abc123");
            assertThat(token.getEmail()).isEqualTo("user@example.com");
            assertThat(token.getExpiryDate()).isEqualTo(FIXED_NOW.plusHours(1));
            assertThat(token.isUsed()).isFalse();
        }
    }
}
