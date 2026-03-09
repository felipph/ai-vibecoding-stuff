package com.example.auth.service;

/**
 * Service for sending emails.
 */
public interface EmailService {

    /**
     * Sends a password reset email to the specified address.
     * @param email the recipient email address
     * @param resetToken the password reset token
     */
    void sendPasswordResetEmail(String email, String resetToken);
}
