package com.microservice.microchatuserservice.application.gateways;

public interface CacheTokenGateway {
    void cacheInvalidToken(String accessToken);

    boolean checkIfTokenIsInvalid(String accessToken);
}
