package com.microservice.microchatuserservice.infrastructure.gateways;

import com.microservice.microchatuserservice.application.gateways.ResetPasswordGateway;
import com.microservice.microchatuserservice.domain.User;
import com.microservice.microchatuserservice.infrastructure.persistence.mappers.UserMapper;
import com.microservice.microchatuserservice.infrastructure.persistence.ResetPasswordRepository;
import com.microservice.microchatuserservice.infrastructure.persistence.entities.ResetPasswordTokenEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ResetPasswordRepositoryGateway implements ResetPasswordGateway {
    private final ResetPasswordRepository tokenRepository;
    private final UserMapper userMapper;

    @Override
    public void createResetPasswordTokenForUser(User user, String token) {
        ResetPasswordTokenEntity myToken = ResetPasswordTokenEntity.builder()
                .token(token)
                .user(userMapper.domainToEntity(user))
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        tokenRepository.save(myToken);
    }

    @Override
    public Optional<User> getUserByResetPasswordToken(String token) {
        return tokenRepository.findByToken(token)
                .map(ResetPasswordTokenEntity::getUser)
                .map(userMapper::entityToDomain);
    }

    @Override
    public void deleteResetPasswordToken(String token) {
        tokenRepository.findByToken(token).ifPresent(tokenRepository::delete);
    }

    @Override
    public boolean validateResetPasswordToken(String token) {
        return tokenRepository.findByToken(token)
                .map(t -> !t.getExpiryDate().isBefore(LocalDateTime.now()))
                .orElse(false);
    }
}
