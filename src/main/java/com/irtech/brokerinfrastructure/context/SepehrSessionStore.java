package com.irtech.brokerinfrastructure.context;


import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class SepehrSessionStore {

    private final AtomicReference<String> sessionKey = new AtomicReference<>();

    public void setSessionKey(String sessionKey) {
        this.sessionKey.set(sessionKey);
    }

    public String getSessionKey() {
        return sessionKey.get();
    }

    public boolean hasSession() {
        String value = sessionKey.get();
        return value != null && !value.isBlank();
    }

    public void clear() {
        sessionKey.set(null);
    }
}