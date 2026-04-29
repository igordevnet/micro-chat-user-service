package com.microservice.microchatuserservice.infrastructure.config;

import com.microservice.microchatuserservice.application.gateways.CacheTokenGateway;
import com.microservice.microchatuserservice.application.gateways.TokenGateway;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenGateway tokenGateway;
    private final CacheTokenGateway cacheTokenGateway;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");

        Cookie[] cookies = request.getCookies();

        String refreshToken = null;

        if (cookies != null) {
            refreshToken = Arrays.stream(cookies)
                    .filter(c -> "refreshToken".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        final String accessToken;

        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return;
        }

        accessToken = authHeader.substring(7);

        tokenGateway.logout(refreshToken);
        cacheTokenGateway.cacheInvalidToken(accessToken);

        SecurityContextHolder.clearContext();
    }
}
