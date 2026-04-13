package com.microservice.microchatuserservice.infrastructure.persistence;

import com.microservice.microchatuserservice.infrastructure.persistence.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findUserByEmail(String email);

    Optional<UserEntity> findUserByUsername(String username);
}
