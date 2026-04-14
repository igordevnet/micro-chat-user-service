package com.microservice.microchatuserservice.application.usecases;

import com.microservice.microchatuserservice.application.Exceptions.InvalidVerifyCodeException;
import com.microservice.microchatuserservice.application.Exceptions.UserNotFoundException;
import com.microservice.microchatuserservice.application.gateways.EmailGateway;
import com.microservice.microchatuserservice.application.gateways.UserGateway;
import com.microservice.microchatuserservice.application.gateways.VerifyEmailGateway;
import com.microservice.microchatuserservice.domain.User;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static com.microservice.microchatuserservice.application.usecases.VerificationCodeGenerator.generateCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final UserGateway userGateway;
    private final VerifyEmailGateway emailChecker;
    private final EmailGateway emailSender;

    @Async
    public void createVerifyEmailCode(String email) {
        String code = generateCode();
        emailChecker.createVerifyEmailCode(email, code);

        log.info("Email code created for user: {}", email);

        try {
            sendEmailChecker(email, code);
        } catch (MessagingException | IOException e) {
            log.error("Error occurred while trying to send email");
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private void sendEmailChecker(String email, String code) throws MessagingException, IOException {
        emailSender.sendVerificationEmail(email, code);
    }

    @Transactional
    public void changeUserStatus(String code, String email) {
        if (!emailChecker.validateEmailCode(code)) {
            throw new InvalidVerifyCodeException("This code is not valid");
        }

        User user = userGateway.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        boolean isEmailValid = emailChecker.getEmailByCode(code)
                .filter(associatedEmail -> associatedEmail.equals(email))
                .isPresent();

        if (isEmailValid) {
            user.verifyEmail();
            userGateway.register(user);
        } else {
            throw new InvalidVerifyCodeException("This code is not valid");
        }

        emailChecker.deleteVerifyEmailCode(code);
    }

    @Transactional
    public void resendVerificationCode(String email) {
        User user = userGateway.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.isEmailVerified()) {
            if (this.emailChecker.getCodeByEmail(email).isPresent()) {
                emailChecker.updateVerifyEmailCode(email, generateCode());
            }
        }
    }
}
