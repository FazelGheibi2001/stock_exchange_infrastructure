package com.irtech.brokerinfrastructure.order;

import com.irtech.brokerinfrastructure.dto.BrokerApiResponse;
import com.irtech.brokerinfrastructure.dto.CalculateOrderRequest;
import com.irtech.brokerinfrastructure.dto.CloseOrderDTO;
import com.irtech.brokerinfrastructure.dto.OrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/broker")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/orders/calculate")
    public ResponseEntity<String> calculateOrder(@RequestBody CalculateOrderRequest request) {
        return ResponseEntity.ok(orderService.calculateOrder(request));
    }

    @PostMapping("/open/position")
    public ResponseEntity<String> newOrder(@RequestBody OrderDTO request) {
        BrokerApiResponse response = orderService.newOrder(request);

        return ResponseEntity
                .status(response.status())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.body());
    }

    @PostMapping("/close/position")
    public ResponseEntity<String> closeOrder(@RequestBody CloseOrderDTO request) {
        BrokerApiResponse response = orderService.closeOrder(request);

        return ResponseEntity
                .status(response.status())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.body());
    }
}
