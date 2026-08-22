package com.irtech.brokerinfrastructure.brokerage.sepehr.session;

import com.irtech.brokerinfrastructure.brokerage.sepehr.SepehrProperties;
import com.irtech.brokerinfrastructure.brokerage.sepehr.auth.LoginService;
import com.irtech.brokerinfrastructure.dto.sepehr.BrokerLoginRequest;
import com.irtech.brokerinfrastructure.dto.sepehr.LoginResponse;
import com.irtech.brokerinfrastructure.redis.models.session.LoginRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;

@Service
@RequiredArgsConstructor
@Slf4j
public class SepehrSessionRecoveryService {

    private final LoginRedisService loginRedisService;
    private final SepehrSessionRegistry sessionRegistry;
    private final LoginService loginService;
    private final SepehrSessionLock sessionLock;
    private final SepehrProperties properties;

    public void recover(String loginName) {
        Lock lock =
                sessionLock.get(loginName);


        if (!lock.tryLock()) {

            log.warn(
                    "Recovery already running. user={}",
                    loginName
            );

            return;
        }

        try {

            log.warn(
                    "Recovering Sepehr session. user={}",
                    loginName
            );


            loginRedisService.delete(loginName);

            sessionRegistry.remove(loginName);

            SepehrProperties.Account account =
                    properties.getAccounts()
                            .stream()
                            .filter(a ->
                                    a.getLoginName()
                                            .equals(loginName)
                            )
                            .findFirst()
                            .orElseThrow();


            BrokerLoginRequest request = new BrokerLoginRequest();

            request.setLoginName(account.getLoginName());
            request.setPassword(account.getPassword());

            LoginResponse response = loginService.loginTOSepehr(request);

            if (response == null || !response.success()) {

                throw new IllegalStateException(
                        "Sepehr recovery failed"
                );
            }


            log.info(
                    "Sepehr recovery successful. user={}",
                    loginName
            );
        } finally {
            lock.unlock();
        }

    }

}
