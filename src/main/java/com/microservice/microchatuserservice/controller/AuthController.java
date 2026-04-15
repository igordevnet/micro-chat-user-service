package com.microservice.microchatuserservice.controller;

import com.microservice.microchatuserservice.application.usecases.AuthUseCase;
import com.microservice.microchatuserservice.application.usecases.EmailService;
import com.microservice.microchatuserservice.application.usecases.ResetPasswordService;
import com.microservice.microchatuserservice.controller.dto.request.*;
import com.microservice.microchatuserservice.controller.dto.response.LoginResponse;
import com.microservice.microchatuserservice.controller.dto.response.RegisterResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;
    private final ResetPasswordService passwordService;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody @Valid RegisterRequest registerRequest
    ) {
        RegisterResponse registerResponse = authUseCase.register(registerRequest);

        return new ResponseEntity<>(registerResponse, HttpStatus.CREATED);
    }

    @PostMapping("/local/signin")
    public ResponseEntity<LoginResponse> login(
            @RequestBody @Valid LoginRequest loginRequest
    ) {
        return ResponseEntity.ok(authUseCase.login(loginRequest));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(authUseCase.refreshToken(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ResetPasswordRequest request) {
        passwordService.createResetPasswordTokenForUser(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordSubmitRequest request) {
        passwordService.changeUserPassword(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestBody @Valid VerifyEmailRequest request) {
        emailService.changeUserStatus(request.code(), request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-email-code")
    public ResponseEntity<Void> resendEmailCode(@RequestBody @Valid ResendEmailCodeRequest request) {
        emailService.resendVerificationCode(request.email());
        return ResponseEntity.ok().build();
    }
}
