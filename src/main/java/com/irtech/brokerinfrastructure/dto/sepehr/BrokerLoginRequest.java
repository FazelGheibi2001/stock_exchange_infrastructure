package com.irtech.brokerinfrastructure.dto.sepehr;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BrokerLoginRequest {
    private String loginName;
    private String password;
    private Integer captchaValue;
}