package com.irtech.brokerinfrastructure.dto;

public record BrokerApiResponse(
        int status,
        String body
) {
}