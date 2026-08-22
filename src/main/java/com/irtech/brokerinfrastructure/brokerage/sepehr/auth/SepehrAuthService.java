package com.irtech.brokerinfrastructure.brokerage.sepehr.auth;


import com.irtech.brokerinfrastructure.dto.sepehr.BrokerLoginRequest;
import com.irtech.brokerinfrastructure.dto.sepehr.LoginResponse;
import com.irtech.brokerinfrastructure.dto.sepehr.SepehrLoginPayload;
import com.irtech.brokerinfrastructure.redis.models.session.LoginRedisModel;
import com.irtech.brokerinfrastructure.redis.models.session.LoginRedisService;
import com.irtech.brokerinfrastructure.redis.models.session.RedisCookie;

import com.irtech.brokerinfrastructure.brokerage.sepehr.session.SepehrSessionContext;
import com.irtech.brokerinfrastructure.brokerage.sepehr.session.SepehrSessionRegistry;
import lombok.RequiredArgsConstructor;

import org.apache.hc.client5.http.cookie.Cookie;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@RequiredArgsConstructor
public class SepehrAuthService {

    private final LoginRedisService loginRedisService;
    private final SepehrSessionRegistry sessionRegistry;

    private static final String IDENTITY_BASE_URL =
            "https://identity-gs.ephoenix.ir";


    private static final Pattern AUTH_COOKIE_PATTERN =
            Pattern.compile(
                    "^otauth-\\d+-(OMS[0-9a-fA-F-]+)$"
            );


    public LoginResponse login(
            String tempSessionId,
            BrokerLoginRequest request
    ) {


        SepehrSessionContext context =
                sessionRegistry.get(
                        tempSessionId
                );


        if (context.getCaptchaHash() == null ||
                context.getCaptchaSalt() == null) {


            throw new IllegalStateException(
                    "Captcha not initialized"
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
                        .body(
                                payload
                        )
                        .retrieve()
                        .toEntity(
                                String.class
                        );


        Optional<Cookie> authCookie =
                context.getCookieStore()
                        .getCookies()
                        .stream()
                        .filter(cookie ->
                                cookie.getName()
                                        .startsWith("otauth-")
                        )
                        .findFirst();


        if (authCookie.isEmpty()) {


            throw new IllegalStateException(
                    "Auth cookie not found after login"
            );
        }


        String xSessionId =
                extractXSessionId(
                        authCookie.get()
                                .getName()
                );


        List<RedisCookie> cookies =
                context.getCookieStore()
                        .getCookies()
                        .stream()
                        .map(cookie ->
                                new RedisCookie(
                                        cookie.getName(),
                                        cookie.getValue(),
                                        cookie.getDomain(),
                                        cookie.getPath()
                                )
                        )
                        .toList();







        /*
            مهم:
            Context موقت captcha را تبدیل می‌کنیم
            به context دائمی کاربر
        */

        sessionRegistry.bindToUser(
                tempSessionId,
                request.getLoginName()
        );


        LoginRedisModel model =
                new LoginRedisModel(

                        request.getLoginName(),

                        xSessionId,

                        cookies,

                        true,

                        Instant.now()
                );


        loginRedisService.save(
                model
        );


        context.setCaptchaHash(null);

        context.setCaptchaSalt(null);


        return new LoginResponse(

                true,

                request.getLoginName(),

                xSessionId,

                response.getStatusCode()
                        .value()
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
                    "Cannot extract x-sessionId"
            );
        }


        return matcher.group(1);

    }

}