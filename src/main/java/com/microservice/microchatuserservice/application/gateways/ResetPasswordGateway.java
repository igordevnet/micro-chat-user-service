package com.microservice.microchatuserservice.application.gateways;

import com.microservice.microchatuserservice.domain.User;

import java.util.Optional;

public interface ResetPasswordGateway {
    void createResetPasswordTokenForUser(User user, String token);

    Optional<User> getUserByResetPasswordToken(String token);

    void deleteResetPasswordToken(String token);

    boolean validateResetPasswordToken(String token);
}
