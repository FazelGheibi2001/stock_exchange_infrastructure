package com.irtech.brokerinfrastructure.config.redis;

import org.springframework.data.redis.core.RedisTemplate;

public abstract class AbstractRedisService {

    public <T> void set(
            RedisTemplate<String, T> redis,
            String key,
            T value) {

        redis.opsForValue()
                .set(key, value);
    }

    public <T> T get(
            RedisTemplate<String, T> redis,
            String key) {

        return redis.opsForValue()
                .get(key);
    }
}
