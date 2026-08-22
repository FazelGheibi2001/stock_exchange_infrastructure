package com.irtech.brokerinfrastructure.brokerage.sepehr.session;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.web.client.RestClient;

import java.util.stream.Collectors;

@Getter
@Setter
@RequiredArgsConstructor
public class SepehrSessionContext {
    private final String sessionId;
    private final BasicCookieStore cookieStore;
    private final CloseableHttpClient httpClient;
    private final RestClient restClient;
    private String captchaHash;
    private String captchaSalt;


    public String cookieHeader(){

        return cookieStore
                .getCookies()
                .stream()
                .map(cookie ->
                        cookie.getName()
                                + "="
                                + cookie.getValue()
                )
                .collect(Collectors.joining("; "));
    }
}