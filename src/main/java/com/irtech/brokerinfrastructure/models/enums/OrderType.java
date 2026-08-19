package com.irtech.brokerinfrastructure.models.enums;

public enum OrderType {
    BUY(1),
    SELL(2);

    private final Integer value;

    OrderType(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
