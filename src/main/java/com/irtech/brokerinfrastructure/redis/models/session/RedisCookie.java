package com.irtech.brokerinfrastructure.redis.models.session;

public record RedisCookie(
        String name,
        String value,
        String domain,
        String path
) {
}