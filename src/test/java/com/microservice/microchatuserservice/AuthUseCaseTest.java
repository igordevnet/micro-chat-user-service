package com.microservice.microchatuserservice;

import com.microservice.microchatuserservice.application.exceptions.EmailAlreadyInUseException;
import com.microservice.microchatuserservice.application.exceptions.InvalidCookieException;
import com.microservice.microchatuserservice.application.exceptions.InvalidCredentialsException;
import com.microservice.microchatuserservice.application.exceptions.UsernameAlreadyInUseException;
import com.microservice.microchatuserservice.application.gateways.TokenGateway;
import com.microservice.microchatuserservice.application.gateways.UserGateway;
import com.microservice.microchatuserservice.application.usecases.AuthUseCase;
import com.microservice.microchatuserservice.application.usecases.EmailService;
import com.microservice.microchatuserservice.controller.dto.request.LoginRequest;
import com.microservice.microchatuserservice.controller.dto.request.RegisterRequest;
import com.microservice.microchatuserservice.controller.dto.response.AuthResponse;
import com.microservice.microchatuserservice.controller.dto.response.RegisterResponse;
import com.microservice.microchatuserservice.domain.Role;
import com.microservice.microchatuserservice.domain.User;
import com.microservice.microchatuserservice.infrastructure.config.JwtService;
import com.microservice.microchatuserservice.infrastructure.config.UserDetailsAdapter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest {

    @InjectMocks
    private AuthUseCase authUseCase;

    @Mock
    private UserGateway userGateway;

    @Mock
    private TokenGateway tokenGateway;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @Test
    @DisplayName("Should successfully register a new user")
    void shouldRegisterSuccessfully() {
        RegisterRequest request = new RegisterRequest("test@example.com", "testUser", "password", 25);
        User savedUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testUser")
                .build();

        when(userGateway.existsUserByEmail(request.email())).thenReturn(false);
        when(userGateway.existsUserByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(userGateway.register(any(User.class))).thenReturn(savedUser);

        RegisterResponse response = authUseCase.register(request);

        assertEquals("testUser", response.username());
        verify(emailService, times(1)).createVerifyEmailCode("test@example.com");
    }

    @Test
    @DisplayName("Should throw EmailAlreadyInUseException during registration")
    void shouldThrowExceptionIfEmailExists() {
        RegisterRequest request = new RegisterRequest("test@example.com", "testUser", "password", 25);

        when(userGateway.existsUserByEmail(request.email())).thenReturn(true);

        assertThrows(EmailAlreadyInUseException.class, () -> authUseCase.register(request));
        verify(userGateway, never()).register(any());
    }

    @Test
    @DisplayName("Should throw UsernameAlreadyInUseException during registration")
    void shouldThrowExceptionIfUsernameExists() {
        RegisterRequest request = new RegisterRequest("test@example.com", "testUser", "password", 25);

        when(userGateway.existsUserByEmail(request.email())).thenReturn(false);
        when(userGateway.existsUserByUsername(request.username())).thenReturn(true);

        assertThrows(UsernameAlreadyInUseException.class, () -> authUseCase.register(request));
        verify(userGateway, never()).register(any());
    }

    @Test
    @DisplayName("Should successfully authenticate user and return tokens")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("testUser", "password");
        HttpServletResponse response = mock(HttpServletResponse.class);

        User user = User.builder().id(1L).username("testUser").role(Role.USER).build();
        UserDetailsAdapter userDetails = new UserDetailsAdapter(user);
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(any(), eq(userDetails))).thenReturn("mockAccessToken");
        when(jwtService.generateRefreshToken()).thenReturn("mockRefreshToken");

        AuthResponse authResponse = authUseCase.login(request, response);

        assertEquals("mockAccessToken", authResponse.accessToken());
        verify(tokenGateway, times(1)).revokeAllUserTokens(user);
        verify(tokenGateway, times(1)).saveUserToken(user, "mockRefreshToken");

        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());
        assertTrue(headerCaptor.getValue().contains("refreshToken=mockRefreshToken"));
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException on bad password")
    void shouldThrowExceptionOnBadCredentials() {
        LoginRequest request = new LoginRequest("testUser", "wrongPassword");
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(InvalidCredentialsException.class, () -> authUseCase.login(request, response));
        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    @DisplayName("Should successfully refresh token using cookie")
    void shouldRefreshTokenSuccessfully() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Cookie cookie = new Cookie("refreshToken", "validOldRefreshToken");

        User user = User.builder().id(1L).username("testUser").role(Role.USER).build();

        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(jwtService.validateRefreshToken("validOldRefreshToken")).thenReturn(user);
        when(jwtService.generateToken(any(), any())).thenReturn("newAccessToken");
        when(jwtService.generateRefreshToken()).thenReturn("newRefreshToken");

        AuthResponse authResponse = authUseCase.refreshToken(request, response);

        assertEquals("newAccessToken", authResponse.accessToken());
        verify(tokenGateway, times(1)).revokeAllUserTokens(user);
        verify(tokenGateway, times(1)).saveUserToken(user, "newRefreshToken");
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
    }

    @Test
    @DisplayName("Should throw InvalidCookieException when no cookies exist")
    void shouldThrowExceptionWhenNoCookies() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getCookies()).thenReturn(null);

        assertThrows(InvalidCookieException.class, () -> authUseCase.refreshToken(request, response));
    }

    @Test
    @DisplayName("Should throw InvalidCookieException when refreshToken cookie is missing")
    void shouldThrowExceptionWhenRefreshTokenCookieMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Cookie wrongCookie = new Cookie("someOtherCookie", "value");

        when(request.getCookies()).thenReturn(new Cookie[]{wrongCookie});

        assertThrows(InvalidCookieException.class, () -> authUseCase.refreshToken(request, response));
    }
}
