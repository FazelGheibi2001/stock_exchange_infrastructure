package com.irtech.brokerinfrastructure.context;

import jakarta.annotation.PreDestroy;

import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SepehrSessionRegistry {

    private final Map<String, SepehrSessionContext> sessions =
            new ConcurrentHashMap<>();

    public SepehrSessionContext create() {

        try {

            String sessionKey = UUID.randomUUID().toString();

            BasicCookieStore cookieStore =
                    new BasicCookieStore();

            // فقط برای Development / Test
            SSLContext sslContext =
                    SSLContexts.custom()
                            .loadTrustMaterial(
                                    null,
                                    TrustAllStrategy.INSTANCE
                            )
                            .build();

            SSLConnectionSocketFactory sslSocketFactory =
                    SSLConnectionSocketFactoryBuilder
                            .create()
                            .setSslContext(sslContext)
                            .setHostnameVerifier(
                                    NoopHostnameVerifier.INSTANCE
                            )
                            .build();

            PoolingHttpClientConnectionManager connectionManager =
                    PoolingHttpClientConnectionManagerBuilder
                            .create()
                            .setSSLSocketFactory(
                                    sslSocketFactory
                            )
                            .build();

            CloseableHttpClient httpClient =
                    HttpClients.custom()
                            .setConnectionManager(
                                    connectionManager
                            )
                            .setDefaultCookieStore(
                                    cookieStore
                            )
                            .build();

            HttpComponentsClientHttpRequestFactory requestFactory =
                    new HttpComponentsClientHttpRequestFactory(
                            httpClient
                    );

            RestClient restClient =
                    RestClient.builder()
                            .requestFactory(requestFactory)
                            .defaultHeader(
                                    HttpHeaders.ACCEPT,
                                    MediaType.APPLICATION_JSON_VALUE
                            )
                            .defaultHeader(
                                    "Origin",
                                    "https://gsmobile.ephoenix.ir"
                            )
                            .defaultHeader(
                                    "Referer",
                                    "https://gsmobile.ephoenix.ir/"
                            )
                            .build();

            SepehrSessionContext context =
                    new SepehrSessionContext(
                            sessionKey,
                            cookieStore,
                            httpClient,
                            restClient
                    );

            sessions.put(sessionKey, context);

            return context;

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to create Sepehr HTTP client",
                    e
            );
        }
    }

    public SepehrSessionContext get(
            String sessionKey
    ) {

        SepehrSessionContext context =
                sessions.get(sessionKey);

        if (context == null) {
            throw new IllegalArgumentException(
                    "Sepehr session not found: " +
                            sessionKey
            );
        }

        return context;
    }

    public void remove(
            String sessionKey
    ) {

        SepehrSessionContext context =
                sessions.remove(sessionKey);

        if (context != null) {
            try {
                context
                        .getHttpClient()
                        .close();
            } catch (IOException ignored) {
            }
        }
    }

    @PreDestroy
    public void shutdown() {

        sessions.values()
                .forEach(context -> {
                    try {
                        context
                                .getHttpClient()
                                .close();
                    } catch (IOException ignored) {
                    }
                });

        sessions.clear();
    }
}