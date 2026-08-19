package com.irtech.brokerinfrastructure.login;

import com.irtech.brokerinfrastructure.dto.BrokerLoginRequest;
import com.irtech.brokerinfrastructure.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/broker")
@RequiredArgsConstructor
public class LoginController {
    private final LoginService service;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> initLogin(@RequestBody BrokerLoginRequest request) {
        return ResponseEntity.ok(service.loginTOSepehr(request));
    }
}
