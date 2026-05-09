package com.microservice.microchatuserservice;

import com.microservice.microchatuserservice.application.exceptions.InvalidVerifyCodeException;
import com.microservice.microchatuserservice.application.exceptions.UserNotFoundException;
import com.microservice.microchatuserservice.application.gateways.EmailGateway;
import com.microservice.microchatuserservice.application.gateways.UserGateway;
import com.microservice.microchatuserservice.application.gateways.VerifyEmailGateway;
import com.microservice.microchatuserservice.application.usecases.EmailService;
import com.microservice.microchatuserservice.application.usecases.VerificationCodeGenerator;
import com.microservice.microchatuserservice.domain.User;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private UserGateway userGateway;

    @Mock
    private VerifyEmailGateway emailChecker;

    @Mock
    private EmailGateway emailSender;

    @Test
    @DisplayName("Should successfully generate a code, save it, and send the email")
    void shouldCreateVerifyEmailCodeSuccessfully() throws MessagingException, IOException {
        String email = "test@example.com";
        String generatedCode = "123456";

        try (MockedStatic<VerificationCodeGenerator> mockedGenerator = mockStatic(VerificationCodeGenerator.class)) {
            mockedGenerator.when(VerificationCodeGenerator::generateCode).thenReturn(generatedCode);

            emailService.createVerifyEmailCode(email);

            verify(emailChecker, times(1)).createVerifyEmailCode(email, generatedCode);
            verify(emailSender, times(1)).sendVerificationEmail(email, generatedCode);
        }
    }

    @Test
    @DisplayName("Should throw RuntimeException if email sending fails")
    void shouldThrowExceptionWhenEmailSendingFails() throws MessagingException, IOException {
        String email = "test@example.com";
        String generatedCode = "123456";

        try (MockedStatic<VerificationCodeGenerator> mockedGenerator = mockStatic(VerificationCodeGenerator.class)) {
            mockedGenerator.when(VerificationCodeGenerator::generateCode).thenReturn(generatedCode);

            doThrow(new IOException("SMTP Error")).when(emailSender).sendVerificationEmail(email, generatedCode);

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    emailService.createVerifyEmailCode(email)
            );

            assertEquals("Failed to send email", exception.getMessage());
        }
    }

    @Test
    @DisplayName("Should successfully verify email and update user status")
    void shouldChangeUserStatusSuccessfully() {
        String email = "test@example.com";
        String code = "123456";
        User user = mock(User.class);

        when(emailChecker.validateEmailCode(code)).thenReturn(true);
        when(userGateway.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(emailChecker.getEmailByCode(code)).thenReturn(Optional.of(email));

        emailService.changeUserStatus(code, email);

        verify(user, times(1)).verifyEmail();
        verify(userGateway, times(1)).register(user);
        verify(emailChecker, times(1)).deleteVerifyEmailCode(code);
    }

    @Test
    @DisplayName("Should throw InvalidVerifyCodeException if code fails primary validation")
    void shouldThrowExceptionIfCodeValidationFails() {
        String email = "test@example.com";
        String code = "INVALID";

        when(emailChecker.validateEmailCode(code)).thenReturn(false);

        assertThrows(InvalidVerifyCodeException.class, () -> emailService.changeUserStatus(code, email));

        verify(userGateway, never()).findUserByEmail(anyString());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException if email does not exist in DB")
    void shouldThrowExceptionIfUserNotFound() {
        String email = "ghost@example.com";
        String code = "123456";

        when(emailChecker.validateEmailCode(code)).thenReturn(true);
        when(userGateway.findUserByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> emailService.changeUserStatus(code, email));
    }

    @Test
    @DisplayName("Should throw InvalidVerifyCodeException if code does not match the provided email")
    void shouldThrowExceptionIfEmailMismatch() {
        String email = "test@example.com";
        String code = "123456";
        User user = mock(User.class);

        when(emailChecker.validateEmailCode(code)).thenReturn(true);
        when(userGateway.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(emailChecker.getEmailByCode(code)).thenReturn(Optional.of("different@example.com"));

        assertThrows(InvalidVerifyCodeException.class, () -> emailService.changeUserStatus(code, email));
        verify(userGateway, never()).register(any());
    }

    @Test
    @DisplayName("Should successfully update the verification code if user is not verified")
    void shouldResendVerificationCodeSuccessfully() {
        String email = "test@example.com";
        String newCode = "999999";
        User user = mock(User.class);

        when(userGateway.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(user.isEmailVerified()).thenReturn(false);
        when(emailChecker.getCodeByEmail(email)).thenReturn(Optional.of("oldCode"));

        try (MockedStatic<VerificationCodeGenerator> mockedGenerator = mockStatic(VerificationCodeGenerator.class)) {
            mockedGenerator.when(VerificationCodeGenerator::generateCode).thenReturn(newCode);

            emailService.resendVerificationCode(email);

            verify(emailChecker, times(1)).updateVerifyEmailCode(email, newCode);
        }
    }

    @Test
    @DisplayName("Should do nothing when resending code if user is already verified")
    void shouldNotResendCodeIfUserAlreadyVerified() {
        String email = "test@example.com";
        User user = mock(User.class);

        when(userGateway.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(user.isEmailVerified()).thenReturn(true);

        emailService.resendVerificationCode(email);

        verify(emailChecker, never()).getCodeByEmail(anyString());
        verify(emailChecker, never()).updateVerifyEmailCode(anyString(), anyString());
    }
}