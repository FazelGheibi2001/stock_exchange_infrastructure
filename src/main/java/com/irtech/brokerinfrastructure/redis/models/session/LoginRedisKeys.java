package com.irtech.brokerinfrastructure.redis.models.session;

import com.irtech.brokerinfrastructure.redis.common.RedisKey;

public final class LoginRedisKeys {

    private LoginRedisKeys() {
    }

    public static String loginInfoKeyBuilder(
            String loginName
    ) {
        return RedisKey.build(
                "auth",
                "sepehr",
                "session",
                loginName
        );
    }

    public static String loginInfoPattern() {

        return RedisKey.build(
                "auth",
                "sepehr",
                "session",
                "*"
        );

    }
}
