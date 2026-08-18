package com.irtech.brokerinfrastructure.dto;

public record SepehrLoginPayload(

        String loginName,

        String password,

        Captcha captcha

) {

    public record Captcha(
            String hash,
            String salt,
            Integer value
    ) {
    }
}
