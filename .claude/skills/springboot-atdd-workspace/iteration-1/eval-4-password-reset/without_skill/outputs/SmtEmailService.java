package com.example.auth.infrastructure.email;

import com.example.auth.domain.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email service implementation using Spring's JavaMailSender.
 */
@Service
public class SmtEmailService implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(SmtEmailService.class);

    private final JavaMailSender mailSender;
    private final String baseUrl;

    public SmtEmailService(JavaMailSender mailSender,
                          @Value("${password.reset.base-url:https://example.com/reset-password}") String baseUrl) {
        this.mailSender = mailSender;
        this.baseUrl = baseUrl;
    }

    @Override
    public void sendPasswordResetEmail(String email, String resetToken) {
        logger.info("Sending password reset email to: {}", email);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password Reset Request");
            message.setText(buildResetEmailBody(resetToken));

            mailSender.send(message);
            logger.info("Password reset email sent successfully to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", email, e);
            // Don't throw - we don't want to fail the reset request
        }
    }

    @Override
    public void sendPasswordChangedConfirmation(String email) {
        logger.info("Sending password changed confirmation to: {}", email);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password Changed Successfully");
            message.setText(buildConfirmationEmailBody());

            mailSender.send(message);
            logger.info("Password changed confirmation sent successfully to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send password changed confirmation to: {}", email, e);
            // Don't throw - we don't want to fail the reset
        }
    }

    private String buildResetEmailBody(String resetToken) {
        String resetLink = baseUrl + "?token=" + resetToken;

        return """
            Hello,

            You have requested to reset your password.

            Click the following link to reset your password:
            %s

            This link will expire in 1 hour.

            If you did not request this password reset, please ignore this email.

            Best regards,
            The Team
            """.formatted(resetLink);
    }

    private String buildConfirmationEmailBody() {
        return """
            Hello,

            Your password has been successfully changed.

            If you did not make this change, please contact support immediately.

            Best regards,
            The Team
            """;
    }
}
