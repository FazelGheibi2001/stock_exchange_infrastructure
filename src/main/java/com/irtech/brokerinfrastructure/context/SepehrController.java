package com.irtech.brokerinfrastructure.context;


import com.irtech.brokerinfrastructure.dto.*;
import com.irtech.brokerinfrastructure.readnumber.ImageNumberReader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/broker/sepehr")
public class SepehrController {

    private final SepehrAuthService authService;

    private final SepehrOrderService orderService;

    public SepehrController(
            SepehrAuthService authService,
            SepehrOrderService orderService
    ) {
        this.authService = authService;
        this.orderService = orderService;
    }

    @GetMapping("/captcha")
    public ResponseEntity<CaptchaResponse> captcha() {

        return ResponseEntity.ok(
                authService.getCaptcha()
        );
    }

    @PostMapping("/{sessionKey}/login")
    public ResponseEntity<LoginResponse> login(
            @PathVariable String sessionKey,
            @RequestBody BrokerLoginRequest request
    ) {

        return ResponseEntity.ok(
                authService.login(
                        sessionKey,
                        request
                )
        );
    }

    @PostMapping("/login/init")
    public ResponseEntity<LoginResponse> initLogin(@RequestBody BrokerLoginRequest request) {

        CaptchaResponse captcha =
                authService.getCaptcha();

        request.setCaptchaValue(ImageNumberReader.readNumber(
                captcha.image(),
                5
        ));

        return ResponseEntity.ok(
                authService.login(
                        captcha.sessionKey(),
                        request
                )
        );
    }

    @PostMapping("/{sessionKey}/orders/calculate")
    public ResponseEntity<String> calculateOrder(
            @PathVariable String sessionKey,
            @RequestBody CalculateOrderRequest request
    ) {

        return ResponseEntity.ok(
                orderService.calculateOrder(
                        sessionKey,
                        request
                )
        );
    }

    @PostMapping("/{sessionKey}/orders/new")
    public ResponseEntity<String> newOrder(
            @PathVariable String sessionKey,
            @RequestBody NewOrderRequest request
    ) {

        SepehrOrderService.BrokerApiResponse response =
                orderService.newOrder(
                        sessionKey,
                        request
                );

        return ResponseEntity
                .status(response.status())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.body());
    }
}