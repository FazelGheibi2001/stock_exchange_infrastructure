package com.irtech.brokerinfrastructure.dto.sepehr;

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