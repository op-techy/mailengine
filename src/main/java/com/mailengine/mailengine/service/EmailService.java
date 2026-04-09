package com.mailengine.mailengine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

/**
 * Thin wrapper around Spring's JavaMailSender.
 * Point spring.mail.* at AWS SES SMTP endpoint in production,
 * or at Mailpit / MailHog in local dev — no other code changes needed.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@mailengine.app}")
    private String fromAddress;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        String link = baseUrl + "/api/auth/verify-email?token=" + token;
        String body = "Welcome to MailEngine!\n\n"
                + "Please verify your email address by clicking the link below:\n\n"
                + link + "\n\n"
                + "The link expires in 24 hours.\n\n"
                + "If you did not sign up, please ignore this email.";
        send(toEmail, "Verify your MailEngine account", body);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        String link = baseUrl + "/reset-password?token=" + token;
        send(toEmail,
                "Reset your MailEngine password",
                "You requested a password reset.\n\n"
                        + "Click the link below to set a new password:\n\n"
                        + link + "\n\n"
                        + "This link expires in 1 hour.\n\n"
                        + "If you did not request this, please ignore this email.");
    }

    @Async
    public void sendInvitationEmail(String toEmail, String tempPassword, String inviterName) {
        send(toEmail,
                "You've been invited to MailEngine",
                "Hi,\n\n"
                        + inviterName + " has invited you to join their MailEngine team.\n\n"
                        + "Your temporary credentials:\n"
                        + "  Email:    " + toEmail + "\n"
                        + "  Password: " + tempPassword + "\n\n"
                        + "Log in at: " + baseUrl + "/login\n\n"
                        + "You will be asked to change your password on first login.");
    }

    @Async
    public void sendAdminPasswordResetEmail(String toEmail, String tempPassword) {
        send(toEmail,
                "Your MailEngine password has been reset",
                "An administrator has reset your password.\n\n"
                        + "Your temporary password: " + tempPassword + "\n\n"
                        + "Log in at: " + baseUrl + "/login\n\n"
                        + "You will be asked to set a new password on login.");
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("Email sent → {} | {}", to, subject);
        } catch (Exception e) {
            // Never rethrow — a failed transactional email must not roll back the calling tx
            log.error("Failed to send email → {} | {} | {}", to, subject, e.getMessage());
        }
    }
}
