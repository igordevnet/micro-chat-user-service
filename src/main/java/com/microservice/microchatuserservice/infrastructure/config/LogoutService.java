package com.microservice.microchatuserservice.infrastructure.config;

import com.microservice.microchatuserservice.application.gateways.TokenGateway;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenGateway tokenGateway;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");

        final String refreshToken;

        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return;
        }

        refreshToken = authHeader.substring(7);

        tokenGateway.logout(refreshToken);

        SecurityContextHolder.clearContext();
    }
}
