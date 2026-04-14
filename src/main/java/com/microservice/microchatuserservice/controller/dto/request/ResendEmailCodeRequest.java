package com.microservice.microchatuserservice.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendEmailCodeRequest(
        @NotBlank
        @Email
        String email
) {}
