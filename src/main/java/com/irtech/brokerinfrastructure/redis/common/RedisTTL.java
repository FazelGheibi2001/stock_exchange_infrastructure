package com.irtech.brokerinfrastructure.redis.common;

import java.time.Duration;

public final class RedisTTL {

    private RedisTTL() {
    }

    public static final Duration MARKET_PRICE = Duration.ofSeconds(5);

    public static final Duration ORDER_BOOK = Duration.ofSeconds(60);

    public static final Duration SESSION = Duration.ofMinutes(30);

}
