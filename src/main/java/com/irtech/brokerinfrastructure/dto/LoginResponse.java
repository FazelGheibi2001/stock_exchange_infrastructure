package com.irtech.brokerinfrastructure.dto;

public record LoginResponse(

        boolean success,

        String sessionKey,

        String xSessionId,

        int brokerHttpStatus

) {
}