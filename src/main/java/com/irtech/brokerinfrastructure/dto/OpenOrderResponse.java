package com.irtech.brokerinfrastructure.dto;

import com.irtech.brokerinfrastructure.models.enums.OrderStatusType;

public record OpenOrderResponse(
        String orderId,
        String serialNumber,
        OrderStatusType status
) {}