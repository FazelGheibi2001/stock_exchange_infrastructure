package com.irtech.brokerinfrastructure.brokerage.sepehr;

import com.irtech.brokerinfrastructure.models.enums.BrokerageType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "sepehr")
public class SepehrProperties {

    private List<Account> accounts;

    @Data
    public static class Account {

        private String name;

        private String loginName;

        private String password;

        private BrokerageType brokerageType;
    }
}