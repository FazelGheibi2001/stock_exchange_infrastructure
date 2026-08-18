package com.irtech.brokerinfrastructure.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.web.client.RestClient;

import java.time.Instant;

@Getter
@RequiredArgsConstructor
public class SepehrSessionContext {

    private final String sessionKey;

    private final BasicCookieStore cookieStore;

    private final CloseableHttpClient httpClient;

    private final RestClient restClient;

    private final Instant createdAt = Instant.now();

    @Setter
    private String captchaHash;

    @Setter
    private String captchaSalt;

    @Setter
    private String xSessionId;

    @Setter
    private boolean authenticated;
}