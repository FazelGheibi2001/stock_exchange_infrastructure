package com.irtech.brokerinfrastructure.web.order;

import com.irtech.brokerinfrastructure.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/broker")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/position/calculate/{loginName}")
    public ResponseEntity<CalculateOrderResponse> calculatePosition(@PathVariable String loginName, @RequestBody CalculateOrderDTO request) {
        return ResponseEntity.ok(orderService.calculatePosition(loginName, request));
    }

    @PostMapping("/position/open/{loginName}")
    public ResponseEntity<String> openPosition(@PathVariable String loginName, @RequestBody OrderDTO request) {
        BrokerApiResponse response = orderService.openPosition(loginName, request);

        return ResponseEntity
                .status(response.status())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.body());
    }

    @PostMapping("/position/close/{loginName}")
    public ResponseEntity<String> closePosition(@PathVariable String loginName, @RequestBody CloseOrderDTO request) {
        BrokerApiResponse response = orderService.closePosition(loginName, request);

        return ResponseEntity
                .status(response.status())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.body());
    }
}
