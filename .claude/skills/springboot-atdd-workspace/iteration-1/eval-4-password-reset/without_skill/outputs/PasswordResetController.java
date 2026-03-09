package com.example.auth.api.controller;

import com.example.auth.api.dto.PasswordResetConfirmRequest;
import com.example.auth.api.dto.PasswordResetRequest;
import com.example.auth.api.dto.TokenValidateRequest;
import com.example.auth.domain.exception.InvalidTokenException;
import com.example.auth.domain.exception.TokenExpiredException;
import com.example.auth.domain.exception.TokenUsedException;
import com.example.auth.domain.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST Controller for password reset operations.
 */
@RestController
@RequestMapping("/api/auth/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    /**
     * Request a password reset for an email address.
     * Always returns success even if email doesn't exist (security best practice).
     *
     * @param request the password reset request containing email
     * @return success message
     */
    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> requestReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestPasswordReset(request.email());

        // Always return same message whether email exists or not (security)
        return ResponseEntity.ok(Map.of(
            "message", "If the email exists, a reset link has been sent"
        ));
    }

    /**
     * Confirm password reset with token and new password.
     *
     * @param request the reset confirmation request
     * @return success message
     * @throws InvalidTokenException if token is invalid
     * @throws TokenExpiredException if token has expired
     * @throws TokenUsedException if token has already been used
     */
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, String>> confirmReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());

        return ResponseEntity.ok(Map.of(
            "message", "Password has been successfully reset"
        ));
    }

    /**
     * Validate if a token is valid.
     *
     * @param request the token validation request
     * @return validation result
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(@Valid @RequestBody TokenValidateRequest request) {
        boolean isValid = passwordResetService.isTokenValid(request.token());

        return ResponseEntity.ok(Map.of("valid", isValid));
    }
}
