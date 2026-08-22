package com.irtech.brokerinfrastructure.dto.sepehr;

public record CaptchaRawResponse(
        String captchaByteData,
        String salt,
        String hashedCaptcha
) {
}
