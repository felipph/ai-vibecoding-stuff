package com.example.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementation of EmailService.
 * In a real application, this would integrate with an email provider like SendGrid, AWS SES, etc.
 * For now, it just logs the email sending action.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Override
    public void sendWelcomeEmail(String email) {
        // In a real application, this would send an actual email
        // For now, we just log it
        log.info("Sending welcome email to: {}", email);

        // Simulated email sending
        // In production: integrate with SendGrid, AWS SES, Mailgun, etc.
    }
}
