package org.example.apigateway.controller;

import org.example.apigateway.dto.TransferRequest;
import org.example.apigateway.service.GatewayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Enumeration;

@RestController
@RequestMapping("/api")
public class TransferController {

    private final GatewayService gatewayService;

    public TransferController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    private org.springframework.http.HttpHeaders getHeaders(HttpServletRequest request) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            headers.addAll(name, Collections.list(request.getHeaders(name)));
        }
        return headers;
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @Valid @RequestBody TransferRequest req,
            HttpServletRequest request) {

        String url = String.format("%s/api/accounts/transfer/%s?from=%d&to=%d&amount=%s",
                gatewayService.getAccountServiceUrl(),
                req.getAccountType().equals("business") ? "business" : "personal",
                req.getFromAccountId(), req.getToAccountId(), req.getAmount());

        return ResponseEntity.ok(gatewayService.proxyPost(url, null, getHeaders(request)));
    }
}