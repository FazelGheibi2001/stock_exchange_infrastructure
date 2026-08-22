package com.irtech.brokerinfrastructure.dto;

import java.math.BigDecimal;

public record CalculateOrderResponse(
        Integer volume,
        BigDecimal totalNetAmount,
        BigDecimal totalFee
) {
}