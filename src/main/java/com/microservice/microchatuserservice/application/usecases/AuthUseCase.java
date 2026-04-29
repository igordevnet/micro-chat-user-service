package com.microservice.microchatuserservice.application.usecases;

import com.microservice.microchatuserservice.application.Exceptions.InvalidCookieException;
import com.microservice.microchatuserservice.application.Exceptions.EmailAlreadyInUseException;
import com.microservice.microchatuserservice.application.Exceptions.InvalidCredentialsException;
import com.microservice.microchatuserservice.application.Exceptions.UsernameAlreadyInUseException;
import com.microservice.microchatuserservice.application.gateways.TokenGateway;
import com.microservice.microchatuserservice.application.gateways.UserGateway;
import com.microservice.microchatuserservice.controller.dto.request.LoginRequest;
import com.microservice.microchatuserservice.controller.dto.request.RegisterRequest;
import com.microservice.microchatuserservice.controller.dto.response.AuthResponse;
import com.microservice.microchatuserservice.controller.dto.response.RegisterResponse;
import com.microservice.microchatuserservice.domain.Role;
import com.microservice.microchatuserservice.domain.User;
import com.microservice.microchatuserservice.infrastructure.config.JwtService;
import com.microservice.microchatuserservice.infrastructure.config.UserDetailsAdapter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthUseCase {

    private final UserGateway userGateway;
    private final TokenGateway tokenGateway;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        throwIfEmailAlreadyExists(request.email());
        throwIfUsernameAlreadyExists(request.username());

        User user = User.builder()
                .email(request.email())
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .age(request.age())
                .role(Role.USER)
                .build();

        var response = userGateway.register(user);

        emailService.createVerifyEmailCode(response.getEmail());

        log.info("New user registered email {}, username: {}", request.email(), request.username());

        return new RegisterResponse(response.getUsername());
    }

    public AuthResponse login(
            LoginRequest request,
            HttpServletResponse response
            ) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );

            UserDetailsAdapter userDetails = (UserDetailsAdapter) authentication.getPrincipal();

            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_USER");

            Map<String, Object> extraClaims = Map.of("role", role, "userId",  userDetails.getUser().getId());

            var accessToken = jwtService.generateToken(extraClaims, userDetails);
            var refreshToken = jwtService.generateRefreshToken();

            User user = userDetails.getUser();
            tokenGateway.revokeAllUserTokens(user);
            tokenGateway.saveUserToken(user, refreshToken);

            var authCookie = generateCookie(refreshToken);
            response.addHeader(HttpHeaders.SET_COOKIE, authCookie.toString());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .build();

        } catch (BadCredentialsException | InternalAuthenticationServiceException ex) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
    }

    public AuthResponse refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = getRefreshTokenFromCookie(request);

        User user = jwtService.validateRefreshToken(refreshToken);

        UserDetails userDetails = new UserDetailsAdapter(user);

        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        Map<String, Object> extraClaims = Map.of("role", role, "userId",  user.getId());

        String newAccessToken = jwtService.generateToken(extraClaims, userDetails);
        String newRefreshToken = jwtService.generateRefreshToken();

        tokenGateway.revokeAllUserTokens(user);
        tokenGateway.saveUserToken(user, newRefreshToken);

        var authCookie = generateCookie(newRefreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, authCookie.toString());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    private void throwIfEmailAlreadyExists(String email) {
        if (userGateway.existsUserByEmail(email) != null) {
            throw new EmailAlreadyInUseException("Email already exists");
        }
    }

    private void throwIfUsernameAlreadyExists(String username) {
        if (userGateway.existsUserByUsername(username) != null) {
            throw new UsernameAlreadyInUseException("Username already exists");
        }
    }

    private ResponseCookie generateCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            throw new InvalidCookieException();
        }

        return Arrays.stream(cookies)
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(InvalidCookieException::new);
    }
}
