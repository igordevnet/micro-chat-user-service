package com.microservice.microchatuserservice.application.usecases;

import com.microservice.microchatuserservice.application.Exceptions.InvalidTokenException;
import com.microservice.microchatuserservice.application.Exceptions.UserNotFoundException;
import com.microservice.microchatuserservice.application.gateways.EmailGateway;
import com.microservice.microchatuserservice.application.gateways.ResetPasswordGateway;
import com.microservice.microchatuserservice.application.gateways.UserGateway;
import com.microservice.microchatuserservice.domain.User;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordService {

    private final UserGateway userGateway;
    private final ResetPasswordGateway resetPassword;
    private final PasswordEncoder passwordEncoder;
    private final EmailGateway emailSender;

    @Value("${app.url}")
    private String appUrl;

    public void createResetPasswordTokenForUser(String email) {
        var user = userGateway.findUserByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });

        String token = UUID.randomUUID().toString();
        resetPassword.createResetPasswordTokenForUser(user, token);

        log.info("Password reset token created for user: {}", email);

        try {
            sendResetTokenEmail(email, token);
        } catch (MessagingException | IOException e) {
            log.error("Error occurred while trying to send email");
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private void sendResetTokenEmail(String email, String token) throws MessagingException, IOException {
        String url = appUrl + "/reset-password-page?token=" + token;
        emailSender.sendResetPasswordEmail(email, url);
    }

    public void changeUserPassword(String token, String newPassword) {
        if (!resetPassword.validateResetPasswordToken(token)) {
            throw new InvalidTokenException();
        }

        User user = resetPassword.getUserByResetPasswordToken(token)
                .orElseThrow(InvalidTokenException::new);

        user.setPassword(passwordEncoder.encode(newPassword));
        userGateway.register(user);
        resetPassword.deleteResetPasswordToken(token);
    }
}