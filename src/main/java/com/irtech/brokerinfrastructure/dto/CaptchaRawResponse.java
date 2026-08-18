package com.irtech.brokerinfrastructure.dto;

public record CaptchaRawResponse(
        String captchaByteData,
        String salt,
        String hashedCaptcha
) {
}
