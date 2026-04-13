package com.microservice.microchatuserservice.controller.dto.request;

import com.microservice.microchatuserservice.domain.Role;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank(message = "The username is required")
        @Size(min = 3, max = 20, message = "The username must have between 3 and 20 chars")
        String username,

        @NotBlank(message = "The password is required")
        @Size(min = 8, message = "The password must have at least 8 chars")
        String password,

        @NotBlank(message = "The email is required")
        @Email(message = "Invalid Email")
        String email,

        @NotNull(message = "The age is required")
        @Min(value = 18, message = "You must be at least 18")
        Integer age
) {
}
