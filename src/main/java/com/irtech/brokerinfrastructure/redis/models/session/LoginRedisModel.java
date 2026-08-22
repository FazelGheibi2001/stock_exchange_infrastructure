package com.irtech.brokerinfrastructure.redis.models.session;

import java.time.Instant;
import java.util.List;

public record LoginRedisModel(
        String loginName,
        String xSessionId,
        List<RedisCookie> cookies,
        boolean authenticated,
        Instant createdAt
){}