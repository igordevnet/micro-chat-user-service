package com.microservice.microchatuserservice.infrastructure.persistence;

import com.microservice.microchatuserservice.infrastructure.persistence.entities.EmailCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerifyEmailRepository extends JpaRepository<EmailCodeEntity, Integer> {
    Optional<EmailCodeEntity> findByCode(String code);
    Optional<EmailCodeEntity> findByEmail(String email);

    @Query("""
        UPDATE EmailCodeEntity e SET e.code = :code
        WHERE e.email = :email
        """)
    @Modifying
    int updateCodeByEmail(String email, String code);
}
