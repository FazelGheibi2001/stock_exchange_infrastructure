package com.irtech.brokerinfrastructure.application.scheduler;

import com.irtech.brokerinfrastructure.brokerage.sepehr.SepehrProperties;
import com.irtech.brokerinfrastructure.brokerage.sepehr.auth.LoginService;
import com.irtech.brokerinfrastructure.brokerage.sepehr.health.SepehrHealthService;
import com.irtech.brokerinfrastructure.dto.sepehr.BrokerLoginRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class SepehrSessionHealthScheduler {

    private final SepehrProperties properties;
    private final SepehrHealthService healthService;
    private final LoginService loginService;

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void healthCheck() {
        properties.getAccounts()
                .forEach(account -> {
                            String username = account.getLoginName();

                            boolean healthy = healthService.check(username);

                            if (healthy) {
                                log.debug(
                                        "Session healthy. user={}",
                                        username
                                );
                                return;
                            }

                            log.warn(
                                    "Session unhealthy. Relogin started. user={}",
                                    username
                            );

                            BrokerLoginRequest request = new BrokerLoginRequest();

                            request.setLoginName(username);
                            request.setPassword(account.getPassword());

                            loginService.loginTOSepehr(request);
                        }
                );

    }

}