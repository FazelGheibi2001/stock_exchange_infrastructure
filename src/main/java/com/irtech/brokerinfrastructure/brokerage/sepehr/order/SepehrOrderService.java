package com.irtech.brokerinfrastructure.brokerage.sepehr.order;


import com.irtech.brokerinfrastructure.brokerage.sepehr.session.SepehrSessionRecoveryService;
import com.irtech.brokerinfrastructure.dto.*;
import com.irtech.brokerinfrastructure.dto.sepehr.CalculateOrderRequest;
import com.irtech.brokerinfrastructure.dto.sepehr.NewOrderRequest;
import com.irtech.brokerinfrastructure.redis.models.session.LoginRedisModel;
import com.irtech.brokerinfrastructure.redis.models.session.LoginRedisService;
import com.irtech.brokerinfrastructure.brokerage.sepehr.session.SepehrSessionContext;
import com.irtech.brokerinfrastructure.brokerage.sepehr.session.SepehrSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class SepehrOrderService {

    private final SepehrSessionRegistry sessionRegistry;
    private final LoginRedisService loginRedisService;
    private final SepehrSessionRecoveryService recoveryService;

    @Value("${sepehr.base.url}")
    private String API_BASE_URL;
    @Value("${sepehr.calculate.url}")
    private String API_CALCULATE_URL;
    @Value("${sepehr.order.url}")
    private String API_ORDER_URL;
    @Value("${sepehr.origin.url}")
    private String ORIGIN_URL;
    @Value("${sepehr.close.url}")
    private String API_CANCEL_ORDER_URL;

    public BrokerApiResponse calculateOrder(
            String loginName,
            CalculateOrderRequest request
    ) {

        LoginRedisModel session = getSession(loginName);

        SepehrSessionContext context = getContext(loginName);

        checkAuthenticated(session);

        try {


            String cookieHeader =
                    context.getCookieStore()
                            .getCookies()
                            .stream()
                            .map(cookie ->
                                    cookie.getName()
                                            + "="
                                            + cookie.getValue()
                            )
                            .collect(Collectors.joining("; "));


            CalculateOrderResponse response = context.getRestClient()
                    .post()
                    .uri(
                            API_BASE_URL +
                                    API_CALCULATE_URL
                    )
                    .header(
                            "x-sessionId",
                            session.xSessionId()
                    )
                    .header(
                            "Cookie",
                            cookieHeader
                    )
                    .header(
                            "Origin",
                            ORIGIN_URL
                    )
                    .header(
                            "Referer",
                            ORIGIN_URL + "/"
                    )
                    .contentType(
                            MediaType.APPLICATION_JSON
                    )
                    .body(
                            request
                    )
                    .retrieve()
                    .body(
                            CalculateOrderResponse.class
                    );

            return new BrokerApiResponse(
                    200,
                    response.toString()
            );
        } catch (RestClientResponseException e) {


            if(e.getStatusCode().value() == 401){

                recoveryService.recover(
                        loginName
                );

                return calculateOrder(
                        loginName,
                        request
                );
            }

            return new BrokerApiResponse(
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );
        }
    }

    public BrokerApiResponse newOrder(String loginName, NewOrderRequest request) {

        LoginRedisModel session = getSession(loginName);

        SepehrSessionContext context = getContext(loginName);

        checkAuthenticated(session);

        try {

            String cookieHeader =
                    context.getCookieStore()
                            .getCookies()
                            .stream()
                            .map(cookie ->
                                    cookie.getName()
                                            + "="
                                            + cookie.getValue()
                            )
                            .collect(Collectors.joining("; "));


            log.info(
                    "Sending new order request. user={}",
                    loginName
            );


            String response =
                    context.getRestClient()
                            .post()
                            .uri(
                                    API_BASE_URL +
                                            API_ORDER_URL
                            )
                            .header(
                                    "x-sessionId",
                                    session.xSessionId()
                            )
                            .header(
                                    "Cookie",
                                    cookieHeader
                            )
                            .header(
                                    "Origin",
                                    ORIGIN_URL
                            )
                            .header(
                                    "Referer",
                                    ORIGIN_URL + "/"
                            )
                            .contentType(
                                    MediaType.APPLICATION_JSON
                            )
                            .body(
                                    request
                            )
                            .retrieve()
                            .body(
                                    String.class
                            );


            return new BrokerApiResponse(
                    200,
                    response
            );


        } catch (RestClientResponseException e) {

            if (e.getStatusCode().value() == 401) {

                recoveryService.recover(loginName);

                return newOrder(
                        loginName,
                        request
                );
            }

            return new BrokerApiResponse(
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );
        }

    }

    public BrokerApiResponse closeOrder(
            String loginName,
            CloseOrderDTO request
    ) {


        LoginRedisModel session =
                getSession(
                        loginName
                );


        SepehrSessionContext context =
                getContext(
                        loginName
                );


        checkAuthenticated(
                session
        );


        try {

            String cookieHeader =
                    context.getCookieStore()
                            .getCookies()
                            .stream()
                            .map(cookie ->
                                    cookie.getName()
                                            + "="
                                            + cookie.getValue()
                            )
                            .collect(Collectors.joining("; "));


            String response =
                    context.getRestClient()
                            .delete()
                            .uri(
                                    API_BASE_URL
                                            + API_CANCEL_ORDER_URL
                                            + "?serialNumber={serialNumber}",
                                    request.getSerialNumber()
                            )
                            .header(
                                    "x-sessionId",
                                    session.xSessionId()
                            )
                            .header(
                                    "Cookie",
                                    cookieHeader
                            )
                            .header(
                                    "Origin",
                                    ORIGIN_URL
                            )
                            .header(
                                    "Referer",
                                    ORIGIN_URL + "/"
                            )
                            .retrieve()
                            .body(
                                    String.class
                            );


            return new BrokerApiResponse(
                    200,
                    response
            );


        } catch (RestClientResponseException e) {


            if (e.getStatusCode().value() == 401) {

                recoveryService.recover(loginName);

                return closeOrder(
                        loginName,
                        request
                );
            }

            return new BrokerApiResponse(
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );

        } catch (Exception e) {


            log.error(
                    "Close order failed. user={}, serialNumber={}",
                    loginName,
                    request.getSerialNumber(),
                    e
            );


            return new BrokerApiResponse(
                    500,
                    e.getMessage()
            );
        }

    }

    private LoginRedisModel getSession(
            String loginName
    ) {


        LoginRedisModel session =
                loginRedisService.find(
                        loginName
                );


        if (session == null) {


            throw new IllegalStateException(
                    "Sepehr session not found for user: "
                            + loginName
            );
        }


        return session;
    }

    private SepehrSessionContext getContext(
            String loginName
    ) {


        return sessionRegistry.get(
                loginName
        );

    }

    private void checkAuthenticated(
            LoginRedisModel session
    ) {


        if (!session.authenticated()) {


            throw new IllegalStateException(
                    "Broker session is not authenticated"
            );
        }


        if (session.xSessionId() == null ||
                session.xSessionId().isBlank()) {


            throw new IllegalStateException(
                    "x-sessionId is missing"
            );
        }

    }

}