package org.example.apigateway.controller;


import org.example.apigateway.service.GatewayService;
import jakarta.servlet.http.HttpServletRequest;
import org.example.apigateway.util.HeaderUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AccountController {

    private final GatewayService gatewayService;

    public AccountController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @GetMapping("/accounts/personal")
    public ResponseEntity<String> getMyPersonalAccounts(HttpServletRequest request) {
        Long clientId = (Long) request.getAttribute("clientId");
        return ResponseEntity.ok(gatewayService.proxyGet(
                gatewayService.getAccountServiceUrl() + "/api/accounts/personal/owner/" + clientId));
    }

    @GetMapping("/accounts/business")
    public ResponseEntity<String> getMyBusinessAccounts(HttpServletRequest request) {
        Long clientId = (Long) request.getAttribute("clientId");
        return ResponseEntity.ok(gatewayService.proxyGet(
                gatewayService.getAccountServiceUrl() + "/api/accounts/business/owner/" + clientId));
    }

    @PostMapping("/accounts/personal")
    public ResponseEntity<String> createPersonalAccount(
            @RequestBody Map<String, Object> body,
            @RequestHeader("X-Manager-Key") String managerKey,
            HttpServletRequest request) {
        Long clientId = (Long) request.getAttribute("clientId");
        String url = String.format("%s/api/accounts/personal?ownerId=%d&currency=%s",
                gatewayService.getAccountServiceUrl(), clientId,
                body.getOrDefault("currency", "RUB"));
        return ResponseEntity.ok(gatewayService.proxyPost(url, body, HeaderUtil.fromRequest(request)));
    }

    @DeleteMapping("/accounts/personal/{id}")
    public ResponseEntity<String> closePersonalAccount(
            @PathVariable Long id,
            @RequestHeader("X-Manager-Key") String managerKey,
            HttpServletRequest request) {
        return ResponseEntity.ok(gatewayService.proxyDelete(
                gatewayService.getAccountServiceUrl() + "/api/accounts/personal/" + id,
                HeaderUtil.fromRequest(request)));
    }
}