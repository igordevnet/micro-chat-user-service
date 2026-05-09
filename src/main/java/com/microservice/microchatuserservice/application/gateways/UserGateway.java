package com.microservice.microchatuserservice.application.gateways;

import com.microservice.microchatuserservice.controller.dto.response.UserResponse;
import com.microservice.microchatuserservice.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserGateway {
    User register(User user);

    Optional<User> findUserByEmail(String email);

    Optional<User> findUserByUsername(String username);

    Boolean existsUserByEmail(String email);

    Boolean existsUserByUsername(String username);

    Page<User> findUsersByUsername(String username, Long userId, Pageable pageable);
}
