package com.microservice.microchatuserservice.controller;

import com.microservice.microchatuserservice.application.usecases.UserUseCase;
import com.microservice.microchatuserservice.controller.dto.response.UserPaginatedResponse;
import com.microservice.microchatuserservice.infrastructure.config.UserDetailsAdapter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "REST Endpoints for searching and discovering users across the ecosystem")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserUseCase userUseCase;

    @Operation(summary = "Search for users", description = "Searches for users by a keyword (matching username or email). Returns a paginated list and automatically excludes the currently authenticated user from the results.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserPaginatedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g., missing keyword or pagination bounds)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT required", content = @Content)
    })
    @GetMapping
    public ResponseEntity<UserPaginatedResponse> searchUsers(
            @Parameter(description = "Keyword to search for in username or email", required = true, example = "igor")
            @RequestParam String username,
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam int page,
            @Parameter(description = "Number of records per page", example = "10")
            @RequestParam int size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsAdapter user
    ) {
        return ResponseEntity.ok(userUseCase.searchUsersByName(username, user.getUser().getId(), page, size));
    }
}