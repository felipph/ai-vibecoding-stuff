package com.example.auth.service;

import com.example.auth.domain.User;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Service layer tests for UserService.
 * Tests business logic for user registration.
 *
 * Acceptance Criteria:
 * [+] User can register with valid email and strong password
 * [+] After successful registration, user receives welcome email
 * [+] User password is stored encrypted in the database
 * [-] System rejects registration with existing email (email must be unique)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest("user@example.com", "ValidPass123");
    }

    @Nested
    @DisplayName("register()")
    class RegisterMethod {

        @Test
        @DisplayName("[+] should save user with valid email and strong password")
        void register_withValidEmailAndStrongPassword_savesUser() {
            // Arrange
            String encodedPassword = "encoded_password";
            User savedUser = new User(validRequest.getEmail(), encodedPassword);
            savedUser = spy(savedUser);
            when(savedUser.getId()).thenReturn(1L);

            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(validRequest.getPassword())).thenReturn(encodedPassword);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // Act
            User result = userService.register(validRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(validRequest.getEmail());
            assertThat(result.getPassword()).isEqualTo(encodedPassword);

            // Verify repository interactions
            verify(userRepository).existsByEmail(validRequest.getEmail());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("[+] should send welcome email after successful registration")
        void register_withValidEmailAndStrongPassword_sendsWelcomeEmail() {
            // Arrange
            String encodedPassword = "encoded_password";
            User savedUser = new User(validRequest.getEmail(), encodedPassword);
            savedUser = spy(savedUser);
            when(savedUser.getId()).thenReturn(1L);

            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(validRequest.getPassword())).thenReturn(encodedPassword);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // Act
            userService.register(validRequest);

            // Assert - verify welcome email was sent
            verify(emailService).sendWelcomeEmail(validRequest.getEmail());
        }

        @Test
        @DisplayName("[+] should store encrypted password in database")
        void register_withValidEmailAndStrongPassword_storesEncryptedPassword() {
            // Arrange
            String rawPassword = validRequest.getPassword();
            String encodedPassword = "encoded_" + rawPassword;
            User savedUser = new User(validRequest.getEmail(), encodedPassword);
            savedUser = spy(savedUser);
            when(savedUser.getId()).thenReturn(1L);

            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // Act
            User result = userService.register(validRequest);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUserToDb = userCaptor.getValue();
            assertThat(savedUserToDb.getPassword()).isEqualTo(encodedPassword);
            assertThat(savedUserToDb.getPassword()).isNotEqualTo(rawPassword);

            // Verify password encoder was called
            verify(passwordEncoder).encode(rawPassword);
        }

        @Test
        @DisplayName("[-] should throw exception when email already exists")
        void register_withExistingEmail_throwsException() {
            // Arrange
            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> userService.register(validRequest))
                .isInstanceOf(UserService.EmailAlreadyExistsException.class)
                .hasMessageContaining("Email already registered")
                .hasMessageContaining(validRequest.getEmail());

            // Verify no save was attempted
            verify(userRepository, never()).save(any(User.class));
            verify(emailService, never()).sendWelcomeEmail(anyString());
        }

        @Test
        @DisplayName("[-] should not send welcome email when registration fails due to duplicate email")
        void register_withExistingEmail_doesNotSendWelcomeEmail() {
            // Arrange
            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> userService.register(validRequest))
                .isInstanceOf(UserService.EmailAlreadyExistsException.class);

            // Verify email service was never called
            verify(emailService, never()).sendWelcomeEmail(anyString());
        }

        @Test
        @DisplayName("should check email existence before attempting save")
        void register_shouldCheckEmailExistenceFirst() {
            // Arrange
            String encodedPassword = "encoded_password";
            User savedUser = new User(validRequest.getEmail(), encodedPassword);
            savedUser = spy(savedUser);
            when(savedUser.getId()).thenReturn(1L);

            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(validRequest.getPassword())).thenReturn(encodedPassword);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // Act
            userService.register(validRequest);

            // Assert - verify order of operations using InOrder
            var inOrder = inOrder(userRepository, passwordEncoder, emailService);
            inOrder.verify(userRepository).existsByEmail(validRequest.getEmail());
            inOrder.verify(passwordEncoder).encode(validRequest.getPassword());
            inOrder.verify(userRepository).save(any(User.class));
            inOrder.verify(emailService).sendWelcomeEmail(validRequest.getEmail());
        }
    }
}
