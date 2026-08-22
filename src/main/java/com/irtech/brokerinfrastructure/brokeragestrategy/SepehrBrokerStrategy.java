package com.irtech.brokerinfrastructure.brokeragestrategy;

import com.irtech.brokerinfrastructure.brokerage.sepehr.order.SepehrOrderService;
import com.irtech.brokerinfrastructure.brokeragestrategy.common.BrokerStrategy;
import com.irtech.brokerinfrastructure.dto.*;
import com.irtech.brokerinfrastructure.dto.sepehr.CalculateOrderRequest;
import com.irtech.brokerinfrastructure.dto.sepehr.NewOrderRequest;
import com.irtech.brokerinfrastructure.models.enums.BrokerageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SepehrBrokerStrategy implements BrokerStrategy {

    private final SepehrOrderService sepehrOrderService;

    @Override
    public BrokerageType type() {
        return BrokerageType.SEPEHR;
    }

    @Override
    public CalculateOrderResponse calculatePosition(String loginName, CalculateOrderDTO dto) {
        CalculateOrderRequest request = convertDtoToCalculateOrderRequest(dto);

        return sepehrOrderService.calculateOrder(loginName, request);
    }

    @Override
    public BrokerApiResponse openPosition(String loginName, OrderDTO dto) {
        NewOrderRequest request = convertDtoToOrderRequest(dto);

        return sepehrOrderService.newOrder(loginName, request);
    }

    @Override
    public BrokerApiResponse closePosition(String loginName, CloseOrderDTO request) {
        return sepehrOrderService.closeOrder(loginName, request);
    }


    private NewOrderRequest convertDtoToOrderRequest(OrderDTO dto) {
        return new NewOrderRequest(
                1,
                null,
                dto.getPrice(),
                dto.getVolume(),
                dto.getType().getValue(),
                dto.getIsin(),
                1
        );
    }


    private CalculateOrderRequest convertDtoToCalculateOrderRequest(CalculateOrderDTO dto) {
        return new CalculateOrderRequest(
                dto.isin(),
                dto.type().getValue(),
                dto.volume(),
                dto.price()
        );
    }
}
