package com.example.auth.service;

import com.example.auth.model.PasswordResetToken;
import com.example.auth.model.User;
import com.example.auth.repository.PasswordResetTokenRepository;
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

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ATDD tests for Password Reset feature.
 *
 * Acceptance Criteria:
 * [+] User can request password reset with registered email
 * [-] If email is not registered, show generic success message (security)
 * [+] System sends reset token valid for 1 hour
 * [+] User can reset password with valid token
 * [-] Expired tokens are rejected
 * [-] Used tokens cannot be reused
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private Clock clock;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private static final String REGISTERED_EMAIL = "user@example.com";
    private static final String UNREGISTERED_EMAIL = "unknown@example.com";
    private static final String VALID_TOKEN = "valid-reset-token-123";
    private static final String NEW_PASSWORD = "NewSecurePassword123!";
    private static final String ENCODED_PASSWORD = "encoded_password_hash";
    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2024, 3, 15, 10, 0, 0);

    private User registeredUser;

    @BeforeEach
    void setUp() {
        registeredUser = User.builder()
                .id(1L)
                .email(REGISTERED_EMAIL)
                .password("oldEncodedPassword")
                .build();

        // Configure fixed clock for repeatable tests
        Clock fixedClock = Clock.fixed(
                FIXED_NOW.atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        );
        passwordResetService.setClock(fixedClock);
    }

    // ========================================================================
    // ACCEPTANCE CRITERION 1: [+] User can request password reset with registered email
    // ACCEPTANCE CRITERION 2: [-] If email is not registered, show generic success message (security)
    // ========================================================================
    @Nested
    @DisplayName("requestPasswordReset")
    class RequestPasswordReset {

        @Test
        @DisplayName("[+] with registered email - creates token and sends email")
        void withRegisteredEmail_createsTokenAndSendsEmail() {
            // Arrange
            when(userRepository.findByEmail(REGISTERED_EMAIL)).thenReturn(Optional.of(registeredUser));
            when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            String result = passwordResetService.requestPasswordReset(REGISTERED_EMAIL);

            // Assert - Token was created and saved
            ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(tokenRepository).save(tokenCaptor.capture());

            PasswordResetToken savedToken = tokenCaptor.getValue();
            assertThat(savedToken.getEmail()).isEqualTo(REGISTERED_EMAIL);
            assertThat(savedToken.getToken()).isNotBlank();
            assertThat(savedToken.isUsed()).isFalse();
            assertThat(savedToken.getExpiryDate()).isEqualTo(FIXED_NOW.plusHours(1));

            // Assert - Email was sent
            verify(emailService).sendPasswordResetEmail(eq(REGISTERED_EMAIL), anyString());

            // Assert - Generic success message returned
            assertThat(result).contains("If your email is registered");
        }

        @Test
        @DisplayName("[-] with unregistered email - returns generic success message (security)")
        void withUnregisteredEmail_returnsGenericSuccessMessage() {
            // Arrange
            when(userRepository.findByEmail(UNREGISTERED_EMAIL)).thenReturn(Optional.empty());

            // Act
            String result = passwordResetService.requestPasswordReset(UNREGISTERED_EMAIL);

            // Assert - No token created for non-existent user
            verify(tokenRepository, never()).save(any());

            // Assert - No email sent
            verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());

            // Assert - Same generic message (security: don't reveal if email exists)
            assertThat(result).contains("If your email is registered");
            assertThat(result).isEqualTo(passwordResetService.requestPasswordReset(REGISTERED_EMAIL));
        }
    }

    // ========================================================================
    // ACCEPTANCE CRITERION 3: [+] System sends reset token valid for 1 hour
    // (Tested implicitly in RequestPasswordReset tests via token expiry validation)
    // ========================================================================

    // ========================================================================
    // ACCEPTANCE CRITERION 4: [+] User can reset password with valid token
    // ========================================================================
    @Nested
    @DisplayName("resetPassword")
    class ResetPassword {

        @Test
        @DisplayName("[+] with valid token - updates password and marks token used")
        void withValidToken_updatesPasswordAndMarksTokenUsed() {
            // Arrange
            PasswordResetToken validToken = PasswordResetToken.builder()
                    .token(VALID_TOKEN)
                    .email(REGISTERED_EMAIL)
                    .expiryDate(FIXED_NOW.plusMinutes(30)) // Valid for 30 more minutes
                    .used(false)
                    .build();

            when(tokenRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(validToken));
            when(userRepository.findByEmail(REGISTERED_EMAIL)).thenReturn(Optional.of(registeredUser));
            when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            passwordResetService.resetPassword(VALID_TOKEN, NEW_PASSWORD);

            // Assert - Password was updated
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPassword()).isEqualTo(ENCODED_PASSWORD);

            // Assert - Token was marked as used
            ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(tokenRepository).save(tokenCaptor.capture());
            assertThat(tokenCaptor.getValue().isUsed()).isTrue();

            // Verify encode was called with the new password
            verify(passwordEncoder).encode(NEW_PASSWORD);
        }

        // ========================================================================
        // ACCEPTANCE CRITERION 5: [-] Expired tokens are rejected
        // ========================================================================
        @Test
        @DisplayName("[-] with expired token - throws exception")
        void withExpiredToken_throwsException() {
            // Arrange
            PasswordResetToken expiredToken = PasswordResetToken.builder()
                    .token("expired-token")
                    .email(REGISTERED_EMAIL)
                    .expiryDate(FIXED_NOW.minusMinutes(1)) // Expired 1 minute ago
                    .used(false)
                    .build();

            when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

            // Act & Assert
            assertThatThrownBy(() -> passwordResetService.resetPassword("expired-token", NEW_PASSWORD))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("expired");

            // Verify password was NOT updated
            verify(userRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(anyString());
        }

        // ========================================================================
        // ACCEPTANCE CRITERION 6: [-] Used tokens cannot be reused
        // ========================================================================
        @Test
        @DisplayName("[-] with already used token - throws exception")
        void withUsedToken_throwsException() {
            // Arrange
            PasswordResetToken usedToken = PasswordResetToken.builder()
                    .token("used-token")
                    .email(REGISTERED_EMAIL)
                    .expiryDate(FIXED_NOW.plusMinutes(30)) // Still valid time-wise
                    .used(true) // But already used
                    .build();

            when(tokenRepository.findByToken("used-token")).thenReturn(Optional.of(usedToken));

            // Act & Assert
            assertThatThrownBy(() -> passwordResetService.resetPassword("used-token", NEW_PASSWORD))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("already been used");

            // Verify password was NOT updated
            verify(userRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("[-] with non-existent token - throws exception")
        void withNonExistentToken_throwsException() {
            // Arrange
            when(tokenRepository.findByToken("nonexistent")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> passwordResetService.resetPassword("nonexistent", NEW_PASSWORD))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("Invalid");

            // Verify password was NOT updated
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("[-] with token for non-existent user - throws exception")
        void withTokenForNonExistentUser_throwsException() {
            // Arrange
            PasswordResetToken token = PasswordResetToken.builder()
                    .token(VALID_TOKEN)
                    .email(UNREGISTERED_EMAIL)
                    .expiryDate(FIXED_NOW.plusMinutes(30))
                    .used(false)
                    .build();

            when(tokenRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(token));
            when(userRepository.findByEmail(UNREGISTERED_EMAIL)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> passwordResetService.resetPassword(VALID_TOKEN, NEW_PASSWORD))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("User not found");

            // Verify password was NOT updated
            verify(userRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(anyString());
        }
    }
}
