package com.irtech.brokerinfrastructure.application;


import com.irtech.brokerinfrastructure.dto.sepehr.BrokerLoginRequest;
import com.irtech.brokerinfrastructure.redis.models.session.LoginRedisService;
import com.irtech.brokerinfrastructure.brokerage.sepehr.SepehrProperties;
import com.irtech.brokerinfrastructure.brokerage.sepehr.auth.LoginService;
import com.irtech.brokerinfrastructure.brokerage.sepehr.session.SepehrSessionRestoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;


@Component
@RequiredArgsConstructor
@Slf4j
public class SepehrStartupLogin implements ApplicationRunner {

    private final LoginService loginService;
    private final SepehrProperties properties;
    private final LoginRedisService loginRedisService;
    private final SepehrSessionRestoreService recoveryService;

    @Override
    public void run(ApplicationArguments args) {

        recoveryService.restoreAllSessions();
        properties.getAccounts()
                .forEach(account ->

                        CompletableFuture.runAsync(
                                () -> initialize(account)
                        )

                );
    }


    private void initialize(SepehrProperties.Account account) {
        String username = account.getLoginName();
        if (loginRedisService.find(username) != null) {
            log.info("Session already exists. skip login. user={}", username);
            return;
        }
        login(account);
    }

    private void login(SepehrProperties.Account account) {
        BrokerLoginRequest request = new BrokerLoginRequest();

        request.setLoginName(account.getLoginName());

        request.setPassword(account.getPassword());

        loginService.loginTOSepehr(request);
    }

}