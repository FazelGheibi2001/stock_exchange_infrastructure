package com.irtech.brokerinfrastructure.dto;

public record NewOrderRequest(
        Integer validity,
        String validityDate,
        Long price,
        Long volume,
        Integer side,
        String isin,
        Integer accountType
) {
}