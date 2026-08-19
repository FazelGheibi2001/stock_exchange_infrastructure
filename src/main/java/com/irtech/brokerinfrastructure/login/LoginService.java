package com.irtech.brokerinfrastructure.login;

import com.irtech.brokerinfrastructure.context.SepehrAuthService;
import com.irtech.brokerinfrastructure.dto.BrokerLoginRequest;
import com.irtech.brokerinfrastructure.dto.CaptchaResponse;
import com.irtech.brokerinfrastructure.dto.LoginResponse;
import com.irtech.brokerinfrastructure.readnumber.ImageNumberReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {
    private static final int MAX_RETRIES = 10;
    private final SepehrAuthService authService;

    public LoginResponse loginTOSepehr(BrokerLoginRequest request) {

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {

            try {
                CaptchaResponse captcha = authService.getCaptcha();

                int captchaValue = ImageNumberReader.readNumber(
                        captcha.image(),
                        5
                );

                request.setCaptchaValue(captchaValue);

                LoginResponse response = authService.login(
                        captcha.sessionKey(),
                        request
                );

                if (response != null) {
                    return response;
                }

            } catch (Exception e) {
                log.error(
                        "Sepehr login attempt " + attempt + " failed: " + e.getMessage()
                );
            }
        }

        return null;
    }
}
