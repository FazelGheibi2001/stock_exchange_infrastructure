package com.irtech.brokerinfrastructure.context;


import com.irtech.brokerinfrastructure.dto.*;

import org.apache.hc.client5.http.cookie.Cookie;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SepehrAuthService {

    private static final String IDENTITY_BASE_URL =
            "https://identity-gs.ephoenix.ir";

    private static final Pattern AUTH_COOKIE_PATTERN =
            Pattern.compile(
                    "^otauth-\\d+-(OMS[0-9a-fA-F-]+)$"
            );

    private final SepehrSessionRegistry sessionRegistry;

    public SepehrAuthService(
            SepehrSessionRegistry sessionRegistry
    ) {
        this.sessionRegistry = sessionRegistry;
    }

    public CaptchaResponse getCaptcha() {

        SepehrSessionContext context =
                sessionRegistry.create();

        CaptchaRawResponse captcha =
                context.getRestClient()
                        .get()
                        .uri(
                                IDENTITY_BASE_URL +
                                        "/api/Captcha/GetCaptcha"
                        )
                        .retrieve()
                        .body(CaptchaRawResponse.class);

        if (captcha == null) {
            sessionRegistry.remove(
                    context.getSessionKey()
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
                "data:image/jpeg;base64," +
                        captcha.captchaByteData();

        return new CaptchaResponse(
                context.getSessionKey(),
                image
        );
    }

    public LoginResponse login(
            String sessionKey,
            BrokerLoginRequest request
    ) {

        SepehrSessionContext context =
                sessionRegistry.get(sessionKey);

        if (context.getCaptchaHash() == null ||
                context.getCaptchaSalt() == null) {

            throw new IllegalStateException(
                    "Captcha has not been initialized"
            );
        }

        SepehrLoginPayload payload =
                new SepehrLoginPayload(
                        request.getLoginName(),
                        request.getPassword(),
                        new SepehrLoginPayload.Captcha(
                                context.getCaptchaHash(),
                                context.getCaptchaSalt(),
                                request.getCaptchaValue()
                        )
                );

        ResponseEntity<String> response =
                context.getRestClient()
                        .post()
                        .uri(
                                IDENTITY_BASE_URL +
                                        "/api/v2/accounts/login"
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .body(payload)
                        .retrieve()
                        .toEntity(String.class);

        Optional<Cookie> authCookie =
                context.getCookieStore()
                        .getCookies()
                        .stream()
                        .filter(cookie ->
                                cookie
                                        .getName()
                                        .startsWith("otauth-")
                        )
                        .findFirst();

        if (authCookie.isEmpty()) {
            throw new IllegalStateException(
                    "Login succeeded but auth cookie was not found"
            );
        }

        String cookieName =
                authCookie.get().getName();

        String xSessionId =
                extractXSessionId(cookieName);

        context.setXSessionId(xSessionId);
        context.setAuthenticated(true);

        // CAPTCHA دیگر لازم نیست
        context.setCaptchaHash(null);
        context.setCaptchaSalt(null);

        return new LoginResponse(
                true,
                sessionKey,
                xSessionId,
                response.getStatusCode().value()
        );
    }

    private String extractXSessionId(
            String cookieName
    ) {

        Matcher matcher =
                AUTH_COOKIE_PATTERN.matcher(
                        cookieName
                );

        if (!matcher.matches()) {
            throw new IllegalStateException(
                    "Could not extract x-sessionId from auth cookie"
            );
        }

        return matcher.group(1);
    }
}