package com.example.auth.api;

import com.example.auth.domain.model.PasswordResetToken;
import com.example.auth.domain.model.User;
import com.example.auth.domain.repository.PasswordResetTokenRepository;
import com.example.auth.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Password Reset REST API.
 * Tests the complete flow from HTTP request to response.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Password Reset API Integration Tests")
class PasswordResetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordResetTokenRepository tokenRepository;

    @BeforeEach
    void setUp() {
        reset(userRepository, tokenRepository);
    }

    @Nested
    @DisplayName("POST /api/auth/password-reset/request")
    class RequestPasswordResetEndpoint {

        @Test
        @DisplayName("should return 200 for registered email")
        void shouldReturn200ForRegisteredEmail() throws Exception {
            // Arrange
            String email = "user@example.com";
            User user = new User(1L, email, "hashedPassword");

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));

            Map<String, String> request = new HashMap<>();
            request.put("email", email);

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/request")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the email exists, a reset link has been sent"));

            verify(tokenRepository).save(any(PasswordResetToken.class));
        }

        @Test
        @DisplayName("should return 200 even for unregistered email (security)")
        void shouldReturn200EvenForUnregisteredEmail() throws Exception {
            // Arrange
            String email = "nonexistent@example.com";
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            Map<String, String> request = new HashMap<>();
            request.put("email", email);

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/request")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the email exists, a reset link has been sent"));

            // Verify no token was created
            verify(tokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("should return 400 for invalid email format")
        void shouldReturn400ForInvalidEmailFormat() throws Exception {
            // Arrange
            Map<String, String> request = new HashMap<>();
            request.put("email", "invalid-email");

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/request")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when email is missing")
        void shouldReturn400WhenEmailIsMissing() throws Exception {
            // Arrange
            Map<String, String> request = new HashMap<>();

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/request")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/password-reset/confirm")
    class ConfirmPasswordResetEndpoint {

        @Test
        @DisplayName("should reset password with valid token")
        void shouldResetPasswordWithValidToken() throws Exception {
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

            Map<String, String> request = new HashMap<>();
            request.put("token", tokenValue);
            request.put("newPassword", newPassword);

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password has been successfully reset"));

            verify(userRepository).updatePassword(userId, newPassword);
            verify(tokenRepository).markAsUsed(tokenValue);
        }

        @Test
        @DisplayName("should return 400 for expired token")
        void shouldReturn400ForExpiredToken() throws Exception {
            // Arrange
            String tokenValue = "expired-token-123";
            PasswordResetToken expiredToken = new PasswordResetToken(
                1L,
                tokenValue,
                Instant.now().minus(1, ChronoUnit.HOURS)
            );

            when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(expiredToken));

            Map<String, String> request = new HashMap<>();
            request.put("token", tokenValue);
            request.put("newPassword", "newPassword123");

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("TOKEN_EXPIRED"));

            verify(userRepository, never()).updatePassword(any(), any());
        }

        @Test
        @DisplayName("should return 400 for already used token")
        void shouldReturn400ForAlreadyUsedToken() throws Exception {
            // Arrange
            String tokenValue = "used-token-123";
            PasswordResetToken usedToken = new PasswordResetToken(
                1L,
                tokenValue,
                Instant.now().plus(1, ChronoUnit.HOURS),
                true
            );

            when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(usedToken));

            Map<String, String> request = new HashMap<>();
            request.put("token", tokenValue);
            request.put("newPassword", "newPassword123");

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("TOKEN_USED"));

            verify(userRepository, never()).updatePassword(any(), any());
        }

        @Test
        @DisplayName("should return 400 for invalid token")
        void shouldReturn400ForInvalidToken() throws Exception {
            // Arrange
            String tokenValue = "invalid-token-123";
            when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.empty());

            Map<String, String> request = new HashMap<>();
            request.put("token", tokenValue);
            request.put("newPassword", "newPassword123");

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_TOKEN"));

            verify(userRepository, never()).updatePassword(any(), any());
        }

        @Test
        @DisplayName("should return 400 for weak password")
        void shouldReturn400ForWeakPassword() throws Exception {
            // Arrange
            String tokenValue = "valid-token-123";
            PasswordResetToken token = new PasswordResetToken(
                1L,
                tokenValue,
                Instant.now().plus(1, ChronoUnit.HOURS)
            );

            when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

            Map<String, String> request = new HashMap<>();
            request.put("token", tokenValue);
            request.put("newPassword", "123"); // Too weak

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_PASSWORD"));
        }

        @Test
        @DisplayName("should return 400 when token is missing")
        void shouldReturn400WhenTokenIsMissing() throws Exception {
            // Arrange
            Map<String, String> request = new HashMap<>();
            request.put("newPassword", "newPassword123");

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when newPassword is missing")
        void shouldReturn400WhenNewPasswordIsMissing() throws Exception {
            // Arrange
            Map<String, String> request = new HashMap<>();
            request.put("token", "some-token");

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/password-reset/validate")
    class ValidateTokenEndpoint {

        @Test
        @DisplayName("should return 200 with valid=true for valid token")
        void shouldReturn200WithValidTrueForValidToken() throws Exception {
            // Arrange
            String tokenValue = "valid-token-123";
            PasswordResetToken token = new PasswordResetToken(
                1L,
                tokenValue,
                Instant.now().plus(1, ChronoUnit.HOURS)
            );

            when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

            Map<String, String> request = new HashMap<>();
            request.put("token", tokenValue);

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
        }

        @Test
        @DisplayName("should return 200 with valid=false for expired token")
        void shouldReturn200WithValidFalseForExpiredToken() throws Exception {
            // Arrange
            String tokenValue = "expired-token-123";
            PasswordResetToken expiredToken = new PasswordResetToken(
                1L,
                tokenValue,
                Instant.now().minus(1, ChronoUnit.HOURS)
            );

            when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(expiredToken));

            Map<String, String> request = new HashMap<>();
            request.put("token", tokenValue);

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
        }

        @Test
        @DisplayName("should return 200 with valid=false for used token")
        void shouldReturn200WithValidFalseForUsedToken() throws Exception {
            // Arrange
            String tokenValue = "used-token-123";
            PasswordResetToken usedToken = new PasswordResetToken(
                1L,
                tokenValue,
                Instant.now().plus(1, ChronoUnit.HOURS),
                true
            );

            when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(usedToken));

            Map<String, String> request = new HashMap<>();
            request.put("token", tokenValue);

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
        }

        @Test
        @DisplayName("should return 200 with valid=false for non-existent token")
        void shouldReturn200WithValidFalseForNonExistentToken() throws Exception {
            // Arrange
            String tokenValue = "nonexistent-token-123";
            when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.empty());

            Map<String, String> request = new HashMap<>();
            request.put("token", tokenValue);

            // Act & Assert
            mockMvc.perform(post("/api/auth/password-reset/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
        }
    }
}
