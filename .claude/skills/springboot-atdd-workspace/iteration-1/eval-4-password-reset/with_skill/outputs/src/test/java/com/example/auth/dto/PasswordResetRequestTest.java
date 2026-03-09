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
 * Bean Validation tests for PasswordResetRequest DTO.
 */
class PasswordResetRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("email validation")
    class EmailValidation {

        @Test
        @DisplayName("with valid email - passes validation")
        void withValidEmail_passesValidation() {
            // Arrange
            PasswordResetRequest request = new PasswordResetRequest("user@example.com");

            // Act
            Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("with null email - fails validation")
        void withNullEmail_failsValidation() {
            // Arrange
            PasswordResetRequest request = new PasswordResetRequest(null);

            // Act
            Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("required"))).isTrue();
        }

        @Test
        @DisplayName("with empty email - fails validation")
        void withEmptyEmail_failsValidation() {
            // Arrange
            PasswordResetRequest request = new PasswordResetRequest("");

            // Act
            Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("required"))).isTrue();
        }

        @Test
        @DisplayName("with blank email - fails validation")
        void withBlankEmail_failsValidation() {
            // Arrange
            PasswordResetRequest request = new PasswordResetRequest("   ");

            // Act
            Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("with invalid email format - fails validation")
        void withInvalidEmailFormat_failsValidation() {
            // Arrange
            PasswordResetRequest request = new PasswordResetRequest("invalid-email");

            // Act
            Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("email"))).isTrue();
        }

        @Test
        @DisplayName("with email missing domain - fails validation")
        void withEmailMissingDomain_failsValidation() {
            // Arrange
            PasswordResetRequest request = new PasswordResetRequest("user@");

            // Act
            Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("with email missing local part - fails validation")
        void withEmailMissingLocalPart_failsValidation() {
            // Arrange
            PasswordResetRequest request = new PasswordResetRequest("@example.com");

            // Act
            Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
        }
    }
}
