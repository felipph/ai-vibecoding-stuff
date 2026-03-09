package com.example.auth.domain.service;

/**
 * Service interface for sending emails.
 */
public interface EmailService {

    /**
     * Sends a password reset email to the user.
     *
     * @param email the recipient email address
     * @param resetToken the password reset token
     */
    void sendPasswordResetEmail(String email, String resetToken);

    /**
     * Sends a confirmation email after password has been reset.
     *
     * @param email the recipient email address
     */
    void sendPasswordChangedConfirmation(String email);
}
