package com.irtech.brokerinfrastructure.brokerage.sepehr.health;

import com.irtech.brokerinfrastructure.brokerage.sepehr.session.SepehrSessionContext;
import com.irtech.brokerinfrastructure.brokerage.sepehr.session.SepehrSessionRegistry;
import com.irtech.brokerinfrastructure.redis.models.session.LoginRedisModel;
import com.irtech.brokerinfrastructure.redis.models.session.LoginRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;


@Slf4j
@Service
@RequiredArgsConstructor
public class SepehrHealthService {

    private final LoginRedisService loginRedisService;
    private final SepehrSessionRegistry sessionRegistry;

    public boolean check(String loginName) {

        LoginRedisModel session = loginRedisService.find(loginName);

        if (session == null) {
            log.warn(
                    "Health check failed. Redis session missing. user={}",
                    loginName
            );

            return false;
        }

        try {

            SepehrSessionContext context = sessionRegistry.get(loginName);

            context.getRestClient()
                    .get()
                    .uri(
                            "https://backofficeexternal-gs.ephoenix.ir/api/party/getcustomerinfo"
                    )
                    .header(
                            "x-sessionId",
                            session.xSessionId()
                    )
                    .header(
                            "Cookie",
                            context.cookieHeader()
                    )
                    .header(
                            "Referer",
                            "https://gs.ephoenix.ir/"
                    )
                    .header(
                            "Origin",
                            "https://gs.ephoenix.ir"
                    )
                    .retrieve()
                    .body(
                            String.class
                    );

            loginRedisService.refreshTTL(loginName);

            return true;

        } catch (RestClientResponseException e) {


            log.warn(
                    "Sepehr health check failed. user={}, status={}",
                    loginName,
                    e.getStatusCode()
            );

            return false;

        } catch (Exception e) {
            log.error(
                    "Sepehr health check unexpected error. user={}",
                    loginName,
                    e
            );

            return false;

        }
    }

}