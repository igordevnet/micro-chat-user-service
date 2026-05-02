package com.microservice.microchatuserservice.application.gateways;

public interface CacheTokenGateway {
    void cacheInvalidToken(String accessToken);

    boolean isBlacklisted(String accessToken);
}
