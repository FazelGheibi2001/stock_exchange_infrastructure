package com.irtech.brokerinfrastructure.brokerage.sepehr.session;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class SepehrSessionLock {

    private final Map<String, ReentrantLock> locks =
            new ConcurrentHashMap<>();

    public Lock get(String loginName) {
        return locks.computeIfAbsent(
                loginName,
                k -> new ReentrantLock()
        );
    }
}
