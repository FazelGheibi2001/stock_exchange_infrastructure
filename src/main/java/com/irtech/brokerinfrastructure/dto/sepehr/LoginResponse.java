package com.irtech.brokerinfrastructure.dto.sepehr;

public record LoginResponse(

        boolean success,

        String sessionKey,

        String xSessionId,

        int brokerHttpStatus

) {
}