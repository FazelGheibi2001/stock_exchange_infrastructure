package com.irtech.brokerinfrastructure.dto;

public record BrokerApiResponse(
        Integer status,
        String body
) {
}