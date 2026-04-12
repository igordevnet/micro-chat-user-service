package com.microservice.microchatuserservice.application.gateways;

import org.springframework.security.core.userdetails.User;

public interface UserGateway {
    User findUserByEmail(String email);

    User findUserByUsername(String username);

    Boolean existsUserByEmail(String email);

    Boolean existsUserByUsername(String username);
}
