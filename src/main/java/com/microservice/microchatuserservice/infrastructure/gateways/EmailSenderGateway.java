package com.microservice.microchatuserservice.infrastructure.gateways;

import com.microservice.microchatuserservice.application.gateways.EmailGateway;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderGateway implements EmailGateway {

    private final JavaMailSender mailSender;

    @Override
    public void sendVerificationEmail(String to, String code) {
        try {
            String htmlContent = loadHtmlTemplate("static/emailchecker.html")
                    .replace("{{verification_code}}", code);

            sendHtmlEmail(to, "Email Confirmation - CHAT", htmlContent);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendResetPasswordEmail(String to, String url) {
        try {
            String htmlContent = loadHtmlTemplate("static/passwordrecovery.html")
                    .replace("{{recovery_link}}", url);

            sendHtmlEmail(to, "Password Recovery - CHAT", htmlContent);
        } catch (Exception e) {
            log.error("Failed to send reset password email to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String loadHtmlTemplate(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }
}