package com.example.auth.service;

/**
 * Service for sending emails.
 * This is a contract that should be implemented by the infrastructure layer.
 */
public interface EmailService {

    /**
     * Send a welcome email to a newly registered user.
     *
     * @param email the user's email address
     */
    void sendWelcomeEmail(String email);
}
