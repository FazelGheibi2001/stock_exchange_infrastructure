package com.irtech.brokerinfrastructure.web.order;

import com.irtech.brokerinfrastructure.dto.*;
import com.irtech.brokerinfrastructure.brokerage.sepehr.SepehrProperties;
import com.irtech.brokerinfrastructure.models.enums.BrokerageType;
import com.irtech.brokerinfrastructure.brokeragestrategy.common.BrokerStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final BrokerStrategyFactory factory;
    private final SepehrProperties properties;

    public CalculateOrderResponse calculatePosition(String loginName, CalculateOrderDTO dto) {
        List<SepehrProperties.Account> accounts = properties.getAccounts();

        for (SepehrProperties.Account account : accounts) {
            if (account.getBrokerageType() == BrokerageType.SEPEHR) {
                return factory
                        .get(BrokerageType.SEPEHR)
                        .calculatePosition(
                                loginName,
                                dto
                        );
            }
        }
        return null;
    }


    public BrokerApiResponse openPosition(String loginName, OrderDTO dto) {
        List<SepehrProperties.Account> accounts = properties.getAccounts();

        for (SepehrProperties.Account account : accounts) {
            if (account.getBrokerageType() == BrokerageType.SEPEHR) {
                return factory
                        .get(BrokerageType.SEPEHR)
                        .openPosition(
                                loginName,
                                dto
                        );
            }
        }
        return null;
    }

    public BrokerApiResponse closePosition(String loginName, CloseOrderDTO dto) {
        List<SepehrProperties.Account> accounts = properties.getAccounts();

        for (SepehrProperties.Account account : accounts) {
            if (account.getBrokerageType() == BrokerageType.SEPEHR) {
                return factory
                        .get(BrokerageType.SEPEHR)
                        .closePosition(
                                loginName,
                                dto
                        );
            }
        }
        return null;
    }
}
