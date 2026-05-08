package com.microservice.microchatuserservice.controller;

import com.microservice.microchatuserservice.application.usecases.AuthUseCase;
import com.microservice.microchatuserservice.application.usecases.EmailService;
import com.microservice.microchatuserservice.application.usecases.ResetPasswordService;
import com.microservice.microchatuserservice.controller.dto.request.*;
import com.microservice.microchatuserservice.controller.dto.response.AuthResponse;
import com.microservice.microchatuserservice.controller.dto.response.RegisterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and account recovery")
public class AuthController {

    private final AuthUseCase authUseCase;
    private final ResetPasswordService passwordService;
    private final EmailService emailService;

    @Operation(summary = "Register a new user", description = "Creates a new user account and triggers a verification email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Validation error on request body"),
            @ApiResponse(responseCode = "409", description = "Conflict - Email or Username already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody @Valid RegisterRequest registerRequest
    ) {
        RegisterResponse registerResponse = authUseCase.register(registerRequest);
        return new ResponseEntity<>(registerResponse, HttpStatus.CREATED);
    }

    @Operation(summary = "User Login", description = "Authenticates a user via email/username and password. Returns an access token and sets an HttpOnly refresh token cookie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "400", description = "Validation error on request body"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials")
    })
    @PostMapping("/local/signin")
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Valid LoginRequest loginRequest,
            @Parameter(hidden = true) HttpServletResponse response
    ) {
        return ResponseEntity.ok(authUseCase.login(loginRequest, response));
    }

    @Operation(summary = "Refresh Access Token", description = "Generates a new JWT access token using the HttpOnly refresh token cookie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token successfully refreshed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid refresh token cookie")
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    ) {
        return ResponseEntity.ok(authUseCase.refreshToken(request, response));
    }

    @Operation(summary = "Request Password Reset", description = "Generates a reset token and sends a password recovery email to the user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recovery email sent (or ignored if email not found)"),
            @ApiResponse(responseCode = "400", description = "Validation error on request body"),
            @ApiResponse(responseCode = "404", description = "Not Found - User email does not exist")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ResetPasswordRequest request) {
        passwordService.createResetPasswordTokenForUser(request.email());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Submit New Password", description = "Validates the reset token and updates the user's password.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password successfully changed"),
            @ApiResponse(responseCode = "400", description = "Validation error on request body"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired reset token")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordSubmitRequest request) {
        passwordService.changeUserPassword(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Verify Email Address", description = "Validates the 6-digit verification code sent to the user's email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email successfully verified"),
            @ApiResponse(responseCode = "400", description = "Validation error on request body"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired verification code"),
            @ApiResponse(responseCode = "404", description = "Not Found - User does not exist")
    })
    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestBody @Valid VerifyEmailRequest request) {
        emailService.changeUserStatus(request.code(), request.email());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Resend Verification Code", description = "Generates a new verification code and resends the email. Ignored if user is already verified.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New verification email sent"),
            @ApiResponse(responseCode = "400", description = "Validation error on request body"),
            @ApiResponse(responseCode = "404", description = "Not Found - User does not exist")
    })
    @PostMapping("/resend-email-code")
    public ResponseEntity<Void> resendEmailCode(@RequestBody @Valid ResendEmailCodeRequest request) {
        emailService.resendVerificationCode(request.email());
        return ResponseEntity.ok().build();
    }
}