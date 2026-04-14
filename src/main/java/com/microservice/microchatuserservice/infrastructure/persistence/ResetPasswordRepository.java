package com.microservice.microchatuserservice.infrastructure.persistence;

import com.microservice.microchatuserservice.infrastructure.persistence.entities.ResetPasswordTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResetPasswordRepository extends JpaRepository<ResetPasswordTokenEntity, Long> {
    Optional<ResetPasswordTokenEntity> findByToken(String token);
}