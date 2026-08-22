package com.irtech.brokerinfrastructure.brokerage.sepehr.session;


import com.irtech.brokerinfrastructure.redis.models.session.LoginRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class SepehrSessionRestoreService {

    private final LoginRedisService loginRedisService;
    private final SepehrSessionRegistry sessionRegistry;

    public void restoreAllSessions() {

        var sessions = loginRedisService.findAll();

        sessions.forEach(session -> {

            try {

                sessionRegistry.restore(
                        session.loginName(),
                        session.cookies()
                );

                log.info(
                        "Recovered Sepehr session. user={}",
                        session.loginName()
                );


            } catch (Exception e) {

                log.error(
                        "Recovery failed. user={}",
                        session.loginName(),
                        e
                );

            }

        });

    }

}