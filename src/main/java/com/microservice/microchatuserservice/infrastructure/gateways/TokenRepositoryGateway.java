package com.microservice.microchatuserservice.infrastructure.gateways;

import com.microservice.microchatuserservice.application.Exceptions.InvalidCredentialsException;
import com.microservice.microchatuserservice.application.gateways.TokenGateway;
import com.microservice.microchatuserservice.domain.User;
import com.microservice.microchatuserservice.infrastructure.persistence.mappers.UserMapper;
import com.microservice.microchatuserservice.infrastructure.persistence.TokenRepository;
import com.microservice.microchatuserservice.infrastructure.persistence.entities.TokenEntity;
import com.microservice.microchatuserservice.infrastructure.persistence.entities.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenRepositoryGateway implements TokenGateway {

    private final TokenRepository tokenRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void saveUserToken(User user, String token) {
        var tokenEntity = TokenEntity.builder()
                .user(userMapper.domainToEntity(user))
                .token(token)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(tokenEntity);
    }

    @Override
    public void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    @Override
    public User isTokenValid(String token) {
        return tokenRepository.findByToken(token)
                .filter(t -> !t.isExpired())
                .filter(t -> !t.isRevoked())
                .map(TokenEntity::getUser)
                .map(userMapper::entityToDomain)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid token"));
    }

    @Override
    public void logout(String token) {
        var storedToken = tokenRepository.findByToken(token).orElse(null);
        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
        }
    }
}
