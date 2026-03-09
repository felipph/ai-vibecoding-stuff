package com.example.auth.controller;

import com.example.auth.domain.User;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.service.EmailService;
import com.example.auth.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for AuthController.
 * Tests REST endpoint behavior for user registration.
 *
 * Uses @WebMvcTest for testing the web layer in isolation.
 */
@WebMvcTest(AuthController.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailService emailService;

    private final String REGISTER_URL = "/api/auth/register";

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterEndpoint {

        @Test
        @DisplayName("[+] should return 201 CREATED with user details when registration succeeds")
        void register_withValidRequest_returnsCreated() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest("user@example.com", "ValidPass123");
            User user = new User(request.getEmail(), "encoded_password");
            user = spy(user);
            when(user.getId()).thenReturn(1L);

            when(userService.register(any(RegisterRequest.class))).thenReturn(user);

            // Act & Assert
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("[+] should return success message indicating welcome email was sent")
        void register_withValidRequest_returnsSuccessMessageWithEmailSent() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest("user@example.com", "ValidPass123");
            User user = new User(request.getEmail(), "encoded_password");
            user = spy(user);
            when(user.getId()).thenReturn(1L);

            when(userService.register(any(RegisterRequest.class))).thenReturn(user);

            // Act & Assert
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully. Welcome email sent."));
        }

        @Test
        @DisplayName("[-] should return 409 CONFLICT when email already exists")
        void register_withExistingEmail_returnsConflict() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest("existing@example.com", "ValidPass123");

            when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new UserService.EmailAlreadyExistsException("Email already registered: existing@example.com"));

            // Act & Assert
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("Email already registered: existing@example.com"));
        }

        @Test
        @DisplayName("[-] should return 400 BAD REQUEST when email is invalid")
        void register_withInvalidEmail_returnsBadRequest() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest("invalid-email", "ValidPass123");

            // Act & Assert
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("[-] should return 400 BAD REQUEST when password is too short")
        void register_withShortPassword_returnsBadRequest() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest("user@example.com", "Pass1");

            // Act & Assert
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("[-] should return 400 BAD REQUEST when password has no uppercase")
        void register_withPasswordWithoutUppercase_returnsBadRequest() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest("user@example.com", "password123");

            // Act & Assert
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("[-] should return 400 BAD REQUEST when password has no lowercase")
        void register_withPasswordWithoutLowercase_returnsBadRequest() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest("user@example.com", "PASSWORD123");

            // Act & Assert
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("[-] should return 400 BAD REQUEST when password has no number")
        void register_withPasswordWithoutNumber_returnsBadRequest() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest("user@example.com", "PasswordPass");

            // Act & Assert
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("[-] should return 400 BAD REQUEST when email is blank")
        void register_withBlankEmail_returnsBadRequest() throws Exception {
            // Arrange
            String requestBody = "{\"email\":\"\",\"password\":\"ValidPass123\"}";

            // Act & Assert
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("[-] should return 400 BAD REQUEST when password is blank")
        void register_withBlankPassword_returnsBadRequest() throws Exception {
            // Arrange
            String requestBody = "{\"email\":\"user@example.com\",\"password\":\"\"}";

            // Act & Assert
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("[-] should return 400 BAD REQUEST when email is missing")
        void register_withMissingEmail_returnsBadRequest() throws Exception {
            // Arrange
            String requestBody = "{\"password\":\"ValidPass123\"}";

            // Act & Assert
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("[-] should return 400 BAD REQUEST when password is missing")
        void register_withMissingPassword_returnsBadRequest() throws Exception {
            // Arrange
            String requestBody = "{\"email\":\"user@example.com\"}";

            // Act & Assert
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest());
        }
    }

    // Helper method to create a spy
    private <T> T spy(T object) {
        return org.mockito.Mockito.spy(object);
    }
}
