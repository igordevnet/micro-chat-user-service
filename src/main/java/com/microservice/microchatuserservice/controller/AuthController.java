package com.microservice.microchatuserservice.controller;

import com.microservice.microchatuserservice.application.usecases.AuthUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;
}
