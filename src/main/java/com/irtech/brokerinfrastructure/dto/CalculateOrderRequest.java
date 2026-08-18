package com.irtech.brokerinfrastructure.dto;


public record CalculateOrderRequest(
        String isin,
        Integer side,
        Long volume,
        Long price
) {
}