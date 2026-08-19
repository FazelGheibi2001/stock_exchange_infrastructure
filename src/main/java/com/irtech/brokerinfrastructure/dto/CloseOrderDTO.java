package com.irtech.brokerinfrastructure.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CloseOrderDTO implements Serializable {
    private String serialNumber;
}
