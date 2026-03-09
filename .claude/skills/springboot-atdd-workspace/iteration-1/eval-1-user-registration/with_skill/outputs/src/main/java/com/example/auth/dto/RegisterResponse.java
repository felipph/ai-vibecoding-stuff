package com.example.auth.dto;

/**
 * DTO for user registration response.
 */
public class RegisterResponse {

    private Long id;
    private String email;
    private String message;

    // Default constructor
    public RegisterResponse() {
    }

    // Constructor
    public RegisterResponse(Long id, String email, String message) {
        this.id = id;
        this.email = email;
        this.message = message;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
