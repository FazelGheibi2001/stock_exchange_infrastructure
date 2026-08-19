package com.irtech.brokerinfrastructure.order;

import com.irtech.brokerinfrastructure.context.SepehrOrderService;
import com.irtech.brokerinfrastructure.dto.BrokerApiResponse;
import com.irtech.brokerinfrastructure.dto.CalculateOrderRequest;
import com.irtech.brokerinfrastructure.dto.NewOrderRequest;
import com.irtech.brokerinfrastructure.dto.OrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/broker")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/orders/calculate")
    public ResponseEntity<String> calculateOrder(@RequestBody CalculateOrderRequest request) {
        return ResponseEntity.ok(orderService.calculateOrder(request));
    }

    @PostMapping("/orders/new")
    public ResponseEntity<String> newOrder(@RequestBody OrderDTO request) {
        BrokerApiResponse response = orderService.newOrder(request);

        return ResponseEntity
                .status(response.status())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.body());
    }
}
