package com.irtech.brokerinfrastructure.brokerage.sepehr.auth;

import com.irtech.brokerinfrastructure.dto.sepehr.CaptchaRawResponse;
import com.irtech.brokerinfrastructure.dto.sepehr.CaptchaResponse;
import com.irtech.brokerinfrastructure.brokerage.sepehr.session.SepehrSessionContext;
import com.irtech.brokerinfrastructure.brokerage.sepehr.session.SepehrSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaptchaService {
    private final SepehrSessionRegistry sessionRegistry;

    private static final String IDENTITY_BASE_URL =
            "https://identity-gs.ephoenix.ir";

    public CaptchaResponse getCaptcha() {


        String tempSessionId =
                UUID.randomUUID().toString();


        SepehrSessionContext context =
                sessionRegistry.create(
                        tempSessionId
                );


        CaptchaRawResponse captcha =
                context.getRestClient()
                        .get()
                        .uri(
                                IDENTITY_BASE_URL +
                                        "/api/Captcha/GetCaptcha"
                        )
                        .retrieve()
                        .body(
                                CaptchaRawResponse.class
                        );


        if (captcha == null) {


            sessionRegistry.remove(
                    tempSessionId
            );


            throw new IllegalStateException(
                    "Captcha response is empty"
            );
        }


        context.setCaptchaHash(
                captcha.hashedCaptcha()
        );


        context.setCaptchaSalt(
                captcha.salt()
        );


        String image =
                "data:image/jpeg;base64,"
                        +
                        captcha.captchaByteData();


        return new CaptchaResponse(
                tempSessionId,
                image
        );

    }

}
