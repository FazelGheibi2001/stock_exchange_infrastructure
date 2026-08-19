package com.irtech.brokerinfrastructure.dto;

import com.irtech.brokerinfrastructure.models.enums.OrderType;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO implements Serializable {
    private Long price;
    protected Long volume;
    private OrderType type;
    private String isin;
}
