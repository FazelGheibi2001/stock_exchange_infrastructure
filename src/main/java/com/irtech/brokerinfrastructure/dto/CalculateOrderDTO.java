package com.irtech.brokerinfrastructure.dto;

import com.irtech.brokerinfrastructure.models.enums.OrderType;

public record CalculateOrderDTO(
        String isin,
        OrderType type,
        Long volume,
        Long price
) {
}