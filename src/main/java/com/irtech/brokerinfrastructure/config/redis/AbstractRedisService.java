package com.irtech.brokerinfrastructure.config.redis;

import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

public abstract class AbstractRedisService {

    public <T> void set(
            RedisTemplate<String, T> redis,
            String key,
            T value) {

        redis.opsForValue()
                .set(key, value);
    }

    public <T> void set(
            RedisTemplate<String, T> redis,
            String key,
            T value,
            Duration ttl
    ) {

        redis.opsForValue()
                .set(
                        key,
                        value,
                        ttl
                );
    }

    public <T> T get(
            RedisTemplate<String, T> redis,
            String key) {

        return redis.opsForValue()
                .get(key);
    }
}
