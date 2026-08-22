package com.irtech.brokerinfrastructure.brokerage.sepehr.session;

import com.irtech.brokerinfrastructure.redis.models.session.RedisCookie;
import jakarta.annotation.PreDestroy;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class SepehrSessionRegistry {

    private final Map<String, SepehrSessionContext> sessions =
            new ConcurrentHashMap<>();

    public SepehrSessionContext create(String loginName) {

        try {
            BasicCookieStore cookieStore =
                    new BasicCookieStore();

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
                            .setSslContext(
                                    sslContext
                            )
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

            HttpComponentsClientHttpRequestFactory factory =
                    new HttpComponentsClientHttpRequestFactory(
                            httpClient
                    );


            RestClient restClient =
                    RestClient.builder()
                            .requestFactory(factory)
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
                            loginName,
                            cookieStore,
                            httpClient,
                            restClient
                    );

            sessions.put(
                    loginName,
                    context
            );

            return context;

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to create Sepehr session",
                    e
            );

        }
    }

    public SepehrSessionContext get(
            String loginName
    ) {

        SepehrSessionContext context =
                sessions.get(
                        loginName
                );

        if (context == null) {

            throw new IllegalArgumentException(
                    "Session not found for user: "
                            + loginName
            );
        }

        return context;

    }

    public void remove(
            String loginName
    ) {

        SepehrSessionContext context =
                sessions.remove(
                        loginName
                );

        close(context);
    }

    private void close(
            SepehrSessionContext context
    ) {

        if (context != null) {

            try {

                context.getHttpClient()
                        .close();

            } catch (IOException ignored) {

            }

        }

    }


    public void bindToUser(
            String tempSessionId,
            String loginName
    ) {

        SepehrSessionContext context =
                sessions.remove(
                        tempSessionId
                );


        if (context == null) {
            throw new IllegalStateException(
                    "Temp session not found: " + tempSessionId
            );
        }


        sessions.put(
                loginName,
                context
        );
    }

    public SepehrSessionContext restore(
            String loginName,
            List<RedisCookie> cookies) {

        SepehrSessionContext context = create(loginName);

        cookies.forEach(cookie -> {

            BasicClientCookie clientCookie =
                    new BasicClientCookie(
                            cookie.name(),
                            cookie.value()
                    );

            if (cookie.domain() != null) {
                clientCookie.setDomain(
                        cookie.domain()
                );

            }

            if (cookie.path() != null) {

                clientCookie.setPath(
                        cookie.path()
                );

            }

            context.getCookieStore()
                    .addCookie(
                            clientCookie
                    );

        });

        sessions.put(
                loginName,
                context
        );

        return context;
    }


    @PreDestroy
    public void shutdown() {
        sessions.values()
                .forEach(this::close);
        sessions.clear();
    }
}