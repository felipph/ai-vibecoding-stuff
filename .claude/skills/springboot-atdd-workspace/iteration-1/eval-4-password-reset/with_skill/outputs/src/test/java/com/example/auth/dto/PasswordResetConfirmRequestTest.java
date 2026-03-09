package com.example.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bean Validation tests for PasswordResetConfirmRequest DTO.
 */
class PasswordResetConfirmRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("token validation")
    class TokenValidation {

        @Test
        @DisplayName("with valid token - passes validation")
        void withValidToken_passesValidation() {
            // Arrange
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                    "valid-token-123",
                    "ValidPassword123"
            );

            // Act
            Set<ConstraintViolation<PasswordResetConfirmRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("with null token - fails validation")
        void withNullToken_failsValidation() {
            // Arrange
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                    null,
                    "ValidPassword123"
            );

            // Act
            Set<ConstraintViolation<PasswordResetConfirmRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("required"))).isTrue();
        }

        @Test
        @DisplayName("with empty token - fails validation")
        void withEmptyToken_failsValidation() {
            // Arrange
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                    "",
                    "ValidPassword123"
            );

            // Act
            Set<ConstraintViolation<PasswordResetConfirmRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("required"))).isTrue();
        }

        @Test
        @DisplayName("with blank token - fails validation")
        void withBlankToken_failsValidation() {
            // Arrange
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                    "   ",
                    "ValidPassword123"
            );

            // Act
            Set<ConstraintViolation<PasswordResetConfirmRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("newPassword validation")
    class NewPasswordValidation {

        @Test
        @DisplayName("with valid password (8+ chars) - passes validation")
        void withValidPassword_passesValidation() {
            // Arrange
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                    "valid-token",
                    "ValidPassword123"
            );

            // Act
            Set<ConstraintViolation<PasswordResetConfirmRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("with exactly 8 chars - passes validation")
        void withExactly8Chars_passesValidation() {
            // Arrange
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                    "valid-token",
                    "12345678"
            );

            // Act
            Set<ConstraintViolation<PasswordResetConfirmRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("with null password - fails validation")
        void withNullPassword_failsValidation() {
            // Arrange
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                    "valid-token",
                    null
            );

            // Act
            Set<ConstraintViolation<PasswordResetConfirmRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("required"))).isTrue();
        }

        @Test
        @DisplayName("with empty password - fails validation")
        void withEmptyPassword_failsValidation() {
            // Arrange
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                    "valid-token",
                    ""
            );

            // Act
            Set<ConstraintViolation<PasswordResetConfirmRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(2); // NotBlank and Size both fail
        }

        @Test
        @DisplayName("with password less than 8 chars - fails validation")
        void withPasswordLessThan8Chars_failsValidation() {
            // Arrange
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                    "valid-token",
                    "short1"
            );

            // Act
            Set<ConstraintViolation<PasswordResetConfirmRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("8 characters"))).isTrue();
        }

        @Test
        @DisplayName("with 7 chars password - fails validation")
        void with7CharsPassword_failsValidation() {
            // Arrange
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                    "valid-token",
                    "1234567"
            );

            // Act
            Set<ConstraintViolation<PasswordResetConfirmRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("8 characters"))).isTrue();
        }
    }

    @Nested
    @DisplayName("combined validation")
    class CombinedValidation {

        @Test
        @DisplayName("with both fields invalid - reports all violations")
        void withBothFieldsInvalid_reportsAllViolations() {
            // Arrange
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                    "",
                    ""
            );

            // Act
            Set<ConstraintViolation<PasswordResetConfirmRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
        }
    }
}
