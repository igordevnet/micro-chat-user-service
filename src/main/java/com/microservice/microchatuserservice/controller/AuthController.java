package com.microservice.microchatuserservice.controller;

import com.microservice.microchatuserservice.application.usecases.AuthUseCase;
import com.microservice.microchatuserservice.controller.dto.request.LoginRequest;
import com.microservice.microchatuserservice.controller.dto.request.RegisterRequest;
import com.microservice.microchatuserservice.controller.dto.response.LoginResponse;
import com.microservice.microchatuserservice.controller.dto.response.RegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody RegisterRequest registerRequest
    ) {
        RegisterResponse registerResponse = authUseCase.register(registerRequest);

        return new ResponseEntity<>(registerResponse, HttpStatus.CREATED);
    }

    @PostMapping("/local/signin")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest loginRequest
    ) {
        return ResponseEntity.ok(authUseCase.login(loginRequest));
    }
}
