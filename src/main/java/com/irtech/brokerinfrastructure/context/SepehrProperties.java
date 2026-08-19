package com.irtech.brokerinfrastructure.context;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sepehr")
public record SepehrProperties(
        String loginName,
        String password
) {
}