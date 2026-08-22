package com.irtech.brokerinfrastructure.dto.sepehr;

public record CaptchaResponse(
        String sessionKey,
        String image
) {
}