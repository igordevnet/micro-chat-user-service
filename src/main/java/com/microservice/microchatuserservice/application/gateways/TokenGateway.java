package com.microservice.microchatuserservice.application.gateways;

import com.microservice.microchatuserservice.domain.User;

public interface TokenGateway {
    void saveUserToken(User user, String token);

    void revokeAllUserTokens(User user);

    User isTokenValid(String token);

    void logout(String token);
}
