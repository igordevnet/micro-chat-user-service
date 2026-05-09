package com.microservice.microchatuserservice.controller.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record UserPaginatedResponse(
        List<UserResponse> content,
        int currentPage,
        int totalPages,
        long totalElements
) {
}
