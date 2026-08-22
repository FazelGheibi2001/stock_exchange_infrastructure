package com.irtech.brokerinfrastructure.brokeragestrategy.common;

import com.irtech.brokerinfrastructure.dto.*;
import com.irtech.brokerinfrastructure.models.enums.BrokerageType;

public interface BrokerStrategy {
    BrokerageType type();

    BrokerApiResponse calculatePosition(
            String loginName,
            CalculateOrderDTO dto
    );


    BrokerApiResponse openPosition(
            String loginName,
            OrderDTO dto
    );


    BrokerApiResponse closePosition(
            String loginName,
            CloseOrderDTO dto
    );

}
