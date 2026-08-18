package com.irtech.brokerinfrastructure.context;

import com.irtech.brokerinfrastructure.dto.CalculateOrderRequest;
import com.irtech.brokerinfrastructure.dto.NewOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

@Service
@RequiredArgsConstructor
public class SepehrOrderService {

    private static final String API_BASE_URL =
            "https://api-gs.ephoenix.ir";

    private final SepehrSessionRegistry sessionRegistry;


    public String calculateOrder(
            String sessionKey,
            CalculateOrderRequest request
    ) {

        SepehrSessionContext context =
                sessionRegistry.get(sessionKey);

        checkAuthenticated(context);

        return context.getRestClient()
                .post()
                .uri(
                        API_BASE_URL +
                                "/api/v2/orders/CalculateOrderParam"
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


    public BrokerApiResponse newOrder(
            String sessionKey,
            NewOrderRequest request
    ) {

        SepehrSessionContext context =
                sessionRegistry.get(sessionKey);

        checkAuthenticated(context);

        try {

            String response = context.getRestClient()
                    .post()
                    .uri(
                            API_BASE_URL +
                                    "/api/v2/orders/NewOrder"
                    )

                    .header(
                            "x-sessionId",
                            context.getXSessionId()
                    )

                    .header(
                            "Origin",
                            "https://gs.ephoenix.ir"
                    )

                    .header(
                            "Referer",
                            "https://gs.ephoenix.ir/"
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

            // خیلی مهم:
            // خطای واقعی Broker را برمی‌گردانیم
            return new BrokerApiResponse(
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );
        }
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


    public record BrokerApiResponse(
            int status,
            String body
    ) {
    }
}
