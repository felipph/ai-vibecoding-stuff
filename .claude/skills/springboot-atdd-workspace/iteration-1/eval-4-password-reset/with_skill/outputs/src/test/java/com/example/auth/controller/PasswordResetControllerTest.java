package com.example.auth.controller;

import com.example.auth.dto.PasswordResetRequest;
import com.example.auth.dto.PasswordResetConfirmRequest;
import com.example.auth.exception.InvalidTokenException;
import com.example.auth.service.PasswordResetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ATDD Controller tests for Password Reset feature.
 *
 * Tests REST API layer for acceptance criteria:
 * [+] User can request password reset with registered email
 * [-] If email is not registered, show generic success message (security)
 * [+] User can reset password with valid token
 * [-] Expired tokens are rejected
 * [-] Used tokens cannot be reused
 */
@WebMvcTest(PasswordResetController.class)
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PasswordResetService passwordResetService;

    private static final String REGISTERED_EMAIL = "user@example.com";
    private static final String VALID_TOKEN = "valid-reset-token-123";
    private static final String NEW_PASSWORD = "NewSecurePassword123!";

    // ========================================================================
    // ACCEPTANCE CRITERION 1 & 2: Request Password Reset
    // ========================================================================
    @Nested
    @DisplayName("POST /api/auth/password-reset/request")
    class RequestPasswordReset {

        @Test
        @DisplayName("[+] with registered email - returns success message")
        void withRegisteredEmail_returnsSuccessMessage() throws Exception {
            // Arrange
            PasswordResetRequest request = new PasswordResetRequest(REGISTERED_EMAIL);
            String genericMessage = "If your email is registered in our system, you will receive a password reset link.";

            when(passwordResetService.requestPasswordReset(REGISTERED_EMAIL)).thenReturn(genericMessage);

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(genericMessage));

            verify(passwordResetService).requestPasswordReset(REGISTERED_EMAIL);
        }

        @Test
        @DisplayName("[-] with unregistered email - returns same generic message (security)")
        void withUnregisteredEmail_returnsSameGenericMessage() throws Exception {
            // Arrange
            String unregisteredEmail = "unknown@example.com";
            PasswordResetRequest request = new PasswordResetRequest(unregisteredEmail);
            String genericMessage = "If your email is registered in our system, you will receive a password reset link.";

            when(passwordResetService.requestPasswordReset(unregisteredEmail)).thenReturn(genericMessage);

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(genericMessage));

            verify(passwordResetService).requestPasswordReset(unregisteredEmail);
        }

        @Test
        @DisplayName("[-] with invalid email format - returns bad request")
        void withInvalidEmailFormat_returnsBadRequest() throws Exception {
            // Arrange
            PasswordResetRequest request = new PasswordResetRequest("invalid-email");

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // Service should not be called for invalid input
            verify(passwordResetService, never()).requestPasswordReset(anyString());
        }

        @Test
        @DisplayName("[-] with empty email - returns bad request")
        void withEmptyEmail_returnsBadRequest() throws Exception {
            // Arrange
            PasswordResetRequest request = new PasswordResetRequest("");

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(passwordResetService, never()).requestPasswordReset(anyString());
        }
    }

    // ========================================================================
    // ACCEPTANCE CRITERION 4, 5, 6: Reset Password with Token
    // ========================================================================
    @Nested
    @DisplayName("POST /api/auth/password-reset/confirm")
    class ConfirmPasswordReset {

        @Test
        @DisplayName("[+] with valid token - returns success")
        void withValidToken_returnsSuccess() throws Exception {
            // Arrange
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(VALID_TOKEN, NEW_PASSWORD);

            doNothing().when(passwordResetService).resetPassword(VALID_TOKEN, NEW_PASSWORD);

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Password reset successfully"));

            verify(passwordResetService).resetPassword(VALID_TOKEN, NEW_PASSWORD);
        }

        @Test
        @DisplayName("[-] with expired token - returns bad request")
        void withExpiredToken_returnsBadRequest() throws Exception {
            // Arrange
            String expiredToken = "expired-token";
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(expiredToken, NEW_PASSWORD);

            doThrow(new InvalidTokenException("Token has expired"))
                    .when(passwordResetService).resetPassword(expiredToken, NEW_PASSWORD);

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Token has expired"));

            verify(passwordResetService).resetPassword(expiredToken, NEW_PASSWORD);
        }

        @Test
        @DisplayName("[-] with used token - returns bad request")
        void withUsedToken_returnsBadRequest() throws Exception {
            // Arrange
            String usedToken = "used-token";
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(usedToken, NEW_PASSWORD);

            doThrow(new InvalidTokenException("Token has already been used"))
                    .when(passwordResetService).resetPassword(usedToken, NEW_PASSWORD);

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Token has already been used"));

            verify(passwordResetService).resetPassword(usedToken, NEW_PASSWORD);
        }

        @Test
        @DisplayName("[-] with invalid token - returns bad request")
        void withInvalidToken_returnsBadRequest() throws Exception {
            // Arrange
            String invalidToken = "invalid-token";
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(invalidToken, NEW_PASSWORD);

            doThrow(new InvalidTokenException("Invalid token"))
                    .when(passwordResetService).resetPassword(invalidToken, NEW_PASSWORD);

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Invalid token"));

            verify(passwordResetService).resetPassword(invalidToken, NEW_PASSWORD);
        }

        @Test
        @DisplayName("[-] with empty token - returns bad request")
        void withEmptyToken_returnsBadRequest() throws Exception {
            // Arrange
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("", NEW_PASSWORD);

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(passwordResetService, never()).resetPassword(anyString(), anyString());
        }

        @Test
        @DisplayName("[-] with empty password - returns bad request")
        void withEmptyPassword_returnsBadRequest() throws Exception {
            // Arrange
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(VALID_TOKEN, "");

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(passwordResetService, never()).resetPassword(anyString(), anyString());
        }
    }
}
