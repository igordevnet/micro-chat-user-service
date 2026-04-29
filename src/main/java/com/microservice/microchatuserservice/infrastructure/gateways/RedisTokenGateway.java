package com.microservice.microchatuserservice.infrastructure.gateways;

import com.microservice.microchatuserservice.application.gateways.CacheTokenGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisTokenGateway implements CacheTokenGateway {

    private final StringRedisTemplate redisTemplate;
    private static final String TOKEN_PREFIX = "user:token:";

    @Override
    public void cacheInvalidToken(String accessToken) {
        redisTemplate.opsForValue().set(
                TOKEN_PREFIX + accessToken,
                "invalid",
                Duration.ofHours(24)
        );
    }

    @Override
    public boolean checkIfTokenIsInvalid(String accessToken) {
        String result = redisTemplate.opsForValue().get(TOKEN_PREFIX + accessToken);
        return result != null;
    }
}
