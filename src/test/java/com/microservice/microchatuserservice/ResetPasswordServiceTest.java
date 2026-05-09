package com.microservice.microchatuserservice;

import com.microservice.microchatuserservice.application.exceptions.InvalidTokenException;
import com.microservice.microchatuserservice.application.exceptions.UserNotFoundException;
import com.microservice.microchatuserservice.application.gateways.EmailGateway;
import com.microservice.microchatuserservice.application.gateways.ResetPasswordGateway;
import com.microservice.microchatuserservice.application.gateways.UserGateway;
import com.microservice.microchatuserservice.application.usecases.ResetPasswordService;
import com.microservice.microchatuserservice.domain.User;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceTest {

    @InjectMocks
    private ResetPasswordService resetPasswordService;

    @Mock
    private UserGateway userGateway;

    @Mock
    private ResetPasswordGateway resetPasswordGateway;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailGateway emailSender;

    private final String appUrl = "http://localhost:4200";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(resetPasswordService, "appUrl", appUrl);
    }

    @Test
    @DisplayName("Should successfully create token and send reset password email")
    void shouldCreateResetPasswordTokenAndSendEmail() throws MessagingException, IOException {
        String email = "test@example.com";
        User user = mock(User.class);

        when(userGateway.findUserByEmail(email)).thenReturn(Optional.of(user));

        resetPasswordService.createResetPasswordTokenForUser(email);

        verify(resetPasswordGateway, times(1)).createResetPasswordTokenForUser(eq(user), anyString());

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender, times(1)).sendResetPasswordEmail(eq(email), urlCaptor.capture());

        String capturedUrl = urlCaptor.getValue();
        assertTrue(capturedUrl.startsWith(appUrl + "/reset-password-page?token="));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException if email does not exist")
    void shouldThrowExceptionIfUserNotFound() {
        String email = "ghost@example.com";

        when(userGateway.findUserByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                resetPasswordService.createResetPasswordTokenForUser(email)
        );

        verify(resetPasswordGateway, never()).createResetPasswordTokenForUser(any(), anyString());
        verify(emailSender, never()).sendResetPasswordEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw RuntimeException if email sending fails")
    void shouldThrowExceptionIfEmailFailsToSend() throws MessagingException, IOException {
        String email = "test@example.com";
        User user = mock(User.class);

        when(userGateway.findUserByEmail(email)).thenReturn(Optional.of(user));

        doThrow(new IOException("SMTP Error")).when(emailSender).sendResetPasswordEmail(anyString(), anyString());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                resetPasswordService.createResetPasswordTokenForUser(email)
        );

        assertEquals("Failed to send email", exception.getMessage());
    }

    @Test
    @DisplayName("Should successfully change user password and delete token")
    void shouldChangeUserPasswordSuccessfully() {
        String token = "valid-token";
        String newPassword = "newSecurePassword123";
        String encodedPassword = "encodedPasswordHash";

        User user = new User();
        user.setEmail("test@example.com");

        when(resetPasswordGateway.validateResetPasswordToken(token)).thenReturn(true);
        when(resetPasswordGateway.getUserByResetPasswordToken(token)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        resetPasswordService.changeUserPassword(token, newPassword);

        assertEquals(encodedPassword, user.getPassword());
        verify(userGateway, times(1)).register(user);
        verify(resetPasswordGateway, times(1)).deleteResetPasswordToken(token);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException if token validation fails")
    void shouldThrowExceptionIfTokenIsInvalid() {
        String token = "invalid-token";
        String newPassword = "newPassword";

        when(resetPasswordGateway.validateResetPasswordToken(token)).thenReturn(false);

        assertThrows(InvalidTokenException.class, () ->
                resetPasswordService.changeUserPassword(token, newPassword)
        );

        verify(resetPasswordGateway, never()).getUserByResetPasswordToken(anyString());
        verify(userGateway, never()).register(any());
    }

    @Test
    @DisplayName("Should throw InvalidTokenException if token is valid but user is not found")
    void shouldThrowExceptionIfUserNotFoundByToken() {
        String token = "orphaned-token";
        String newPassword = "newPassword";

        when(resetPasswordGateway.validateResetPasswordToken(token)).thenReturn(true);
        when(resetPasswordGateway.getUserByResetPasswordToken(token)).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () ->
                resetPasswordService.changeUserPassword(token, newPassword)
        );

        verify(userGateway, never()).register(any());
    }
}
