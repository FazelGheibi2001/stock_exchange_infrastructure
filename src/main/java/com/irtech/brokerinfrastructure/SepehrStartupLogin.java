package com.irtech.brokerinfrastructure;

import com.irtech.brokerinfrastructure.context.SepehrProperties;
import com.irtech.brokerinfrastructure.context.SepehrSessionStore;
import com.irtech.brokerinfrastructure.dto.BrokerLoginRequest;
import com.irtech.brokerinfrastructure.dto.LoginResponse;
import com.irtech.brokerinfrastructure.login.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SepehrStartupLogin implements ApplicationRunner {

    private final LoginService loginService;
    private final SepehrSessionStore sessionStore;
    private final SepehrProperties properties;

    @Override
    public void run(ApplicationArguments args) {

        log.info("Starting automatic Sepehr login...");

        BrokerLoginRequest request = new BrokerLoginRequest();

        request.setLoginName(properties.loginName());
        request.setPassword(properties.password());

        LoginResponse response = loginService.loginTOSepehr(request);

        if (response == null) {
            log.error("Sepehr automatic login failed after maximum retries");
            System.exit(0);
        }

        if (!response.success()) {
            log.error(
                    "Sepehr login failed. HTTP status: {}",
                    response.brokerHttpStatus()
            );
            System.exit(0);
        }

        if (response.sessionKey() == null || response.sessionKey().isBlank()) {
            log.error("Sepehr login succeeded but sessionKey is empty");
            System.exit(0);
        }

        sessionStore.setSessionKey(response.sessionKey());

        log.info("Sepehr session successfully initialized");
    }
}
