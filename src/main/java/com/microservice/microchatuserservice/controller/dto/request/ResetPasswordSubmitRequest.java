package com.microservice.microchatuserservice.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordSubmitRequest(
        @NotBlank
        String token,

        @NotBlank(message = "The password is required")
        String newPassword
) {
}
