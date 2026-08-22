package com.irtech.brokerinfrastructure.brokerage.sepehr.auth;


import com.irtech.brokerinfrastructure.brokerage.sepehr.session.SepehrSessionLock;
import com.irtech.brokerinfrastructure.dto.sepehr.BrokerLoginRequest;
import com.irtech.brokerinfrastructure.dto.sepehr.CaptchaResponse;
import com.irtech.brokerinfrastructure.dto.sepehr.LoginResponse;
import com.irtech.brokerinfrastructure.ocr.CaptchaOCR;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.stereotype.Service;


import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;


@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final SepehrAuthService authService;
    private final CaptchaService captchaService;
    private final SepehrSessionLock sessionLock;

    private static final int MAX_RETRIES = 5;
    private static final int MIN_DELAY_MS = 1500;
    private static final int MAX_DELAY_MS = 5000;

    public LoginResponse loginTOSepehr(
            BrokerLoginRequest request
    ) {
        boolean locked = false;

        String loginName = request.getLoginName();

        log.info(
                "Waiting for login lock. user={}",
                loginName
        );

        Lock lock = sessionLock.get(loginName);

        try {
            if (!lock.tryLock(60, TimeUnit.SECONDS)) {
                log.warn(
                        "Could not acquire login lock. user={}",
                        loginName
                );
                return null;
            }

            locked = true;

            log.info(
                    "Login lock acquired. user={}",
                    loginName
            );


        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {

            for (int attempt = 1;
                 attempt <= MAX_RETRIES;
                 attempt++) {

                try {

                    CaptchaResponse captcha =
                            captchaService.getCaptcha();

                    int captchaValue =
                            CaptchaOCR.readNumber(
                                    captcha.image(),
                                    5
                            );

                    request.setCaptchaValue(
                            captchaValue
                    );

                    LoginResponse response =
                            authService.login(
                                    captcha.sessionKey(),
                                    request
                            );

                    if (response != null &&
                            response.success()) {


                        log.info(
                                "Sepehr login successful. user={}",
                                request.getLoginName()
                        );

                        return response;

                    }

                } catch (Exception e) {

                    log.error(
                            "Sepehr login attempt {} failed. user={}, error={}",
                            attempt,
                            request.getLoginName(),
                            e.getMessage()
                    );

                }

                if (attempt < MAX_RETRIES) {

                    sleepBeforeRetry(
                            attempt
                    );
                }

            }

            return null;
        } finally {
            if (locked) {
                lock.unlock();

                log.info(
                        "Login lock released. user={}",
                        loginName
                );
            }
        }

    }

    private void sleepBeforeRetry(
            int attempt
    ) {

        int delay =
                ThreadLocalRandom.current()
                        .nextInt(
                                MIN_DELAY_MS,
                                MAX_DELAY_MS + 1
                        );

        delay *= attempt;

        try {

            Thread.sleep(
                    delay
            );

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();
        }
    }
}