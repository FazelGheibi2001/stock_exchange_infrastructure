package com.irtech.brokerinfrastructure.dto;

public record CaptchaResponse(
        String sessionKey,
        String image
) {
}