package com.microservice.microchatuserservice.controller.dto.response;

import lombok.Builder;

@Builder
public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
