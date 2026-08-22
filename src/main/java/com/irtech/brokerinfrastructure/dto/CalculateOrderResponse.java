package com.irtech.brokerinfrastructure.dto;

import lombok.ToString;

import java.math.BigDecimal;

public record CalculateOrderResponse(
        Integer volume,
        BigDecimal totalNetAmount,
        BigDecimal totalFee
) {
}