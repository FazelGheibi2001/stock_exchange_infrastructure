package com.irtech.brokerinfrastructure.redis.common;

public final class RedisKey {

    private RedisKey() {
    }

    public static String build(
            String... parts
    ) {

        return String.join(":", parts);
    }
}
