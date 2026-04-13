package com.microservice.microchatuserservice.application.gateways;

import com.microservice.microchatuserservice.domain.User;

import java.util.Optional;

public interface UserGateway {
    User register(User user);

    Optional<User> findUserByEmail(String email);

    Optional<User> findUserByUsername(String username);

    Boolean existsUserByEmail(String email);

    Boolean existsUserByUsername(String username);
}
