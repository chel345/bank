package org.example.apigateway.controller;

import org.example.apigateway.service.GatewayService;
import org.example.apigateway.service.KafkaCommandService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final GatewayService gatewayService;
    private final KafkaCommandService kafkaService;

    public OrderController(GatewayService gatewayService, KafkaCommandService kafkaService) {
        this.gatewayService = gatewayService;
        this.kafkaService = kafkaService;
    }

    @GetMapping("/orders")
    public ResponseEntity<String> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long clientId = (Long) request.getAttribute("clientId");
        String url = String.format("%s/api/orders/customer/%d?page=%d&size=%d",
                gatewayService.getOrderServiceUrl(), clientId, page, size);
        return ResponseEntity.ok(gatewayService.proxyGet(url));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<String> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(gatewayService.proxyGet(
                gatewayService.getOrderServiceUrl() + "/api/orders/" + id));
    }

    @PostMapping("/orders")
    public ResponseEntity<Map<String, String>> createOrder(
            @RequestBody Map<String, Object> body,
            @RequestHeader("X-Manager-Key") String managerKey,
            HttpServletRequest request) {
        Long clientId = (Long) request.getAttribute("clientId");
        String commandId = kafkaService.createOrder(
                clientId,
                (String) body.get("date"),
                ((Number) body.get("branchId")).longValue(),
                managerKey);
        return ResponseEntity.accepted().body(Map.of(
                "status", "COMMAND_SENT", "commandId", commandId));
    }

    @PutMapping("/orders/{id}")
    public ResponseEntity<Map<String, String>> updateOrder(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader("X-Manager-Key") String managerKey) {
        String commandId = kafkaService.updateOrder(id, body, managerKey);
        return ResponseEntity.accepted().body(Map.of(
                "status", "COMMAND_SENT", "commandId", commandId));
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Map<String, String>> deleteOrder(
            @PathVariable Long id,
            @RequestHeader("X-Manager-Key") String managerKey) {
        String commandId = kafkaService.deleteOrder(id, managerKey);
        return ResponseEntity.accepted().body(Map.of(
                "status", "COMMAND_SENT", "commandId", commandId));
    }
}