package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.dto.ErrorResponse;
import com.example.auth.dto.PasswordResetConfirmRequest;
import com.example.auth.dto.PasswordResetRequest;
import com.example.auth.exception.InvalidTokenException;
import com.example.auth.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for password reset operations.
 */
@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * Requests a password reset for the given email.
     * Always returns success message for security (doesn't reveal if email exists).
     *
     * @param request the password reset request containing email
     * @return generic success message
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        String message = passwordResetService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.of(message));
    }

    /**
     * Confirms a password reset with a valid token.
     *
     * @param request the confirmation request containing token and new password
     * @return success message if reset was successful
     */
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.of("Password reset successfully"));
    }

    /**
     * Exception handler for InvalidTokenException.
     * @param ex the exception
     * @return bad request with error message
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException ex) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(ex.getMessage()));
    }
}
