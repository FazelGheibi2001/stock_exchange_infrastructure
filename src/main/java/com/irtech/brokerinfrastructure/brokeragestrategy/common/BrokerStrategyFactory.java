package com.irtech.brokerinfrastructure.brokeragestrategy.common;

import com.irtech.brokerinfrastructure.models.enums.BrokerageType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BrokerStrategyFactory {


    private final Map<BrokerageType, BrokerStrategy> strategies;


    public BrokerStrategyFactory(
            List<BrokerStrategy> strategies
    ) {

        this.strategies =
                strategies.stream()
                        .collect(
                                Collectors.toMap(
                                        BrokerStrategy::type,
                                        Function.identity()
                                )
                        );
    }


    public BrokerStrategy get(
            BrokerageType type
    ) {

        BrokerStrategy strategy =
                strategies.get(type);


        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Unsupported broker: "
                            + type
            );
        }


        return strategy;
    }
}
