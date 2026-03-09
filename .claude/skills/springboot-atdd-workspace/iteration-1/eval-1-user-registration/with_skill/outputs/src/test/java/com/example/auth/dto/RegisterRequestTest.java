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
 * Bean Validation tests for RegisterRequest.
 * Tests acceptance criteria related to email and password format validation.
 *
 * Acceptance Criteria:
 * [-] System rejects registration with invalid email format
 * [-] System rejects registration with password shorter than 8 characters
 * [-] System rejects registration with password without uppercase letter
 * [-] System rejects registration with password without lowercase letter
 * [-] System rejects registration with password without number
 */
@DisplayName("RegisterRequest Validation Tests")
class RegisterRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Email Validation")
    class EmailValidation {

        @Test
        @DisplayName("[-] should reject registration with blank email")
        void validate_withBlankEmail_failsValidation() {
            // Arrange
            RegisterRequest request = new RegisterRequest("", "ValidPass123");

            // Act
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email is required")))
                .isTrue();
        }

        @Test
        @DisplayName("[-] should reject registration with null email")
        void validate_withNullEmail_failsValidation() {
            // Arrange
            RegisterRequest request = new RegisterRequest(null, "ValidPass123");

            // Act
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email is required")))
                .isTrue();
        }

        @Test
        @DisplayName("[-] should reject registration with invalid email format")
        void validate_withInvalidEmail_failsValidation() {
            // Arrange
            RegisterRequest request = new RegisterRequest("invalid-email", "ValidPass123");

            // Act
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email must be valid")))
                .isTrue();
        }

        @Test
        @DisplayName("[-] should reject registration with email missing @ symbol")
        void validate_withEmailMissingAtSymbol_failsValidation() {
            // Arrange
            RegisterRequest request = new RegisterRequest("userexample.com", "ValidPass123");

            // Act
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email must be valid")))
                .isTrue();
        }

        @Test
        @DisplayName("[+] should accept registration with valid email format")
        void validate_withValidEmail_passesEmailValidation() {
            // Arrange
            RegisterRequest request = new RegisterRequest("user@example.com", "ValidPass123");

            // Act
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Password Validation")
    class PasswordValidation {

        @Test
        @DisplayName("[-] should reject registration with blank password")
        void validate_withBlankPassword_failsValidation() {
            // Arrange
            RegisterRequest request = new RegisterRequest("user@example.com", "");

            // Act
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password is required")))
                .isTrue();
        }

        @Test
        @DisplayName("[-] should reject registration with null password")
        void validate_withNullPassword_failsValidation() {
            // Arrange
            RegisterRequest request = new RegisterRequest("user@example.com", null);

            // Act
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password is required")))
                .isTrue();
        }

        @Test
        @DisplayName("[-] should reject registration with password shorter than 8 characters")
        void validate_withShortPassword_failsValidation() {
            // Arrange - password has only 7 characters but meets other requirements
            RegisterRequest request = new RegisterRequest("user@example.com", "Pass12");

            // Act
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password must be at least 8 characters")))
                .isTrue();
        }

        @Test
        @DisplayName("[-] should reject registration with password without uppercase letter")
        void validate_withPasswordWithoutUppercase_failsValidation() {
            // Arrange - password has 8 chars, number, lowercase but no uppercase
            RegisterRequest request = new RegisterRequest("user@example.com", "password123");

            // Act
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password must be at least 8 characters")))
                .isTrue();
        }

        @Test
        @DisplayName("[-] should reject registration with password without lowercase letter")
        void validate_withPasswordWithoutLowercase_failsValidation() {
            // Arrange - password has 8 chars, number, uppercase but no lowercase
            RegisterRequest request = new RegisterRequest("user@example.com", "PASSWORD123");

            // Act
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password must be at least 8 characters")))
                .isTrue();
        }

        @Test
        @DisplayName("[-] should reject registration with password without number")
        void validate_withPasswordWithoutNumber_failsValidation() {
            // Arrange - password has 8 chars, uppercase, lowercase but no number
            RegisterRequest request = new RegisterRequest("user@example.com", "PasswordPass");

            // Act
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password must be at least 8 characters")))
                .isTrue();
        }

        @Test
        @DisplayName("[+] should accept registration with valid password (8 chars, upper, lower, number)")
        void validate_withValidPassword_passesPasswordValidation() {
            // Arrange
            RegisterRequest request = new RegisterRequest("user@example.com", "ValidPass123");

            // Act
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("[+] should accept registration with exactly 8 character password meeting all requirements")
        void validate_withExactly8CharPassword_passesPasswordValidation() {
            // Arrange - exactly 8 characters meeting all requirements
            RegisterRequest request = new RegisterRequest("user@example.com", "Pass1234");

            // Act
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }
    }
}
