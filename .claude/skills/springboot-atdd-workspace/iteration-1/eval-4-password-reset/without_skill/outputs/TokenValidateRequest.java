package com.example.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for token validation.
 */
public record TokenValidateRequest(
    @NotBlank(message = "Token is required")
    String token
) {
}
