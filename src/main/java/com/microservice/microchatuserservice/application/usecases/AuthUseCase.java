package com.microservice.microchatuserservice.application.usecases;

import com.microservice.microchatuserservice.application.Exceptions.EmailAlreadyInUseException;
import com.microservice.microchatuserservice.application.Exceptions.InvalidCredentialsException;
import com.microservice.microchatuserservice.application.Exceptions.UserNotFoundException;
import com.microservice.microchatuserservice.application.Exceptions.UsernameAlreadyInUseException;
import com.microservice.microchatuserservice.application.gateways.TokenGateway;
import com.microservice.microchatuserservice.application.gateways.UserGateway;
import com.microservice.microchatuserservice.controller.dto.request.LoginRequest;
import com.microservice.microchatuserservice.controller.dto.request.RegisterRequest;
import com.microservice.microchatuserservice.controller.dto.response.LoginResponse;
import com.microservice.microchatuserservice.controller.dto.response.RegisterResponse;
import com.microservice.microchatuserservice.domain.Role;
import com.microservice.microchatuserservice.domain.User;
import com.microservice.microchatuserservice.infrastructure.config.JwtService;
import com.microservice.microchatuserservice.infrastructure.config.UserDetailsAdapter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
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

    public LoginResponse login(LoginRequest request) {
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

            Map<String, Object> extraClaims = new HashMap<>();

            extraClaims.put("role", role);

            var accessToken = jwtService.generateToken(extraClaims, userDetails);
            var refreshToken = jwtService.generateRefreshToken(userDetails);

            User user = userDetails.getUser();
            tokenGateway.revokeAllUserTokens(user);
            tokenGateway.saveUserToken(user, refreshToken);

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (BadCredentialsException | InternalAuthenticationServiceException ex) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
    }

    public LoginResponse refreshToken(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        throwIfHeaderIsEmpty(authHeader);

        String refreshToken = authHeader.substring(7);
        String username = jwtService.extractUsername(refreshToken);

        throwIfUsernameIsEmpty(username);

        User user = userGateway.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        UserDetails userDetails = new UserDetailsAdapter(user);

        throwIfRefreshTokenIsInvalid(refreshToken, userDetails);

        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow();

        Map<String, Object> extraClaims = Map.of("role", role);

        String newAccessToken = jwtService.generateToken(extraClaims, userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        tokenGateway.revokeAllUserTokens(user);
        tokenGateway.saveUserToken(user, newRefreshToken);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void throwIfHeaderIsEmpty(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing refresh token");
        }
    }

    private void throwIfUsernameIsEmpty(String username) {
        if (username == null) {
            log.error("Invalid refresh token");
            throw new InvalidCredentialsException("Invalid refresh token");
        }
    }

    private void throwIfRefreshTokenIsInvalid(String refreshToken, UserDetails userDetails) {
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            log.error("Invalid refresh token");
            throw new InvalidCredentialsException("Invalid refresh token");
        }
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
}
