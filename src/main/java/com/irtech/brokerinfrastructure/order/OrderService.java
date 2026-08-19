package com.irtech.brokerinfrastructure.order;

import com.irtech.brokerinfrastructure.context.SepehrSessionContext;
import com.irtech.brokerinfrastructure.context.SepehrSessionRegistry;
import com.irtech.brokerinfrastructure.context.SepehrSessionStore;
import com.irtech.brokerinfrastructure.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
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


    private final SepehrSessionRegistry sessionRegistry;
    private final SepehrSessionStore sepehrSessionStore;


    public String calculateOrder(CalculateOrderRequest request) {
        String sessionKey = sepehrSessionStore.getSessionKey();

        SepehrSessionContext context = sessionRegistry.get(sessionKey);

        checkAuthenticated(context);

        return context.getRestClient()
                .post()
                .uri(
                        API_BASE_URL + API_CALCULATE_URL
                )
                .header(
                        "x-sessionId",
                        context.getXSessionId()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);
    }


    public BrokerApiResponse newOrder(OrderDTO dto) {
        String sessionKey = sepehrSessionStore.getSessionKey();

        SepehrSessionContext context = sessionRegistry.get(sessionKey);

        checkAuthenticated(context);

        NewOrderRequest request = convertDtoToOrderRequest(dto);

        try {

            String response = context.getRestClient()
                    .post()
                    .uri(
                            API_BASE_URL + API_ORDER_URL
                    )
                    .header(
                            "x-sessionId",
                            context.getXSessionId()
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

                    .body(request)

                    .retrieve()

                    .body(String.class);


            return new BrokerApiResponse(
                    200,
                    response
            );

        } catch (RestClientResponseException e) {
            log.error(e.getResponseBodyAsString());

            return new BrokerApiResponse(
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );
        }
    }

    private NewOrderRequest convertDtoToOrderRequest(OrderDTO dto) {
        return new NewOrderRequest(
                1,
                null,
                dto.getPrice(),
                dto.getVolume(),
                dto.getType().getValue(),
                dto.getIsin(),
                1
        );
    }


    private void checkAuthenticated(
            SepehrSessionContext context
    ) {

        if (!context.isAuthenticated()) {
            throw new IllegalStateException(
                    "Broker session is not authenticated"
            );
        }

        if (context.getXSessionId() == null) {

            throw new IllegalStateException(
                    "x-sessionId is missing"
            );
        }
    }

    public BrokerApiResponse closeOrder(CloseOrderDTO request) {

        String sessionKey = sepehrSessionStore.getSessionKey();

        SepehrSessionContext context = sessionRegistry.get(sessionKey);

        checkAuthenticated(context);

        try {

            String response = context.getRestClient()
                    .delete()
                    .uri(
                            API_BASE_URL
                                    + API_CANCEL_ORDER_URL
                                    + "?serialNumber={serialNumber}",
                            request.getSerialNumber()
                    )
                    .header(
                            "x-sessionId",
                            context.getXSessionId()
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
                    .body(String.class);

            return new BrokerApiResponse(
                    200,
                    response
            );

        } catch (RestClientResponseException e) {

            log.error(
                    "Close order failed. serialNumber={}, status={}, response={}",
                    request.getSerialNumber(),
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );

            return new BrokerApiResponse(
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );

        } catch (Exception e) {

            log.error(
                    "Close order failed. serialNumber={}",
                    request.getSerialNumber(),
                    e
            );

            return new BrokerApiResponse(
                    500,
                    e.getMessage()
            );
        }
    }
}
