package org.example.apigateway.controller;

import org.example.apigateway.service.GatewayService;
import org.example.apigateway.service.KafkaCommandService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CardController {

    private final GatewayService gatewayService;
    private final KafkaCommandService kafkaService;

    public CardController(GatewayService gatewayService, KafkaCommandService kafkaService) {
        this.gatewayService = gatewayService;
        this.kafkaService = kafkaService;
    }

    @GetMapping("/cards")
    public ResponseEntity<String> getMyCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long clientId = (Long) request.getAttribute("clientId");
        // Получаем счета клиента и их карты
        String accountsResponse = gatewayService.proxyGet(
                gatewayService.getAccountServiceUrl() + "/api/accounts/personal/owner/" + clientId);
        // Упрощённо возвращаем как есть
        return ResponseEntity.ok(accountsResponse);
    }

    @GetMapping("/cards/{id}")
    public ResponseEntity<String> getCardById(@PathVariable Long id) {
        return ResponseEntity.ok(gatewayService.proxyGet(
                gatewayService.getCardServiceUrl() + "/api/cards/" + id));
    }

    @PostMapping("/cards")
    public ResponseEntity<Map<String, String>> createCard(
            @RequestBody Map<String, Object> body,
            @RequestHeader("X-Manager-Key") String managerKey) {
        String commandId = kafkaService.createCard(
                ((Number) body.get("accountId")).longValue(),
                (String) body.get("cardNumber"),
                managerKey);
        return ResponseEntity.accepted().body(Map.of(
                "status", "COMMAND_SENT", "commandId", commandId));
    }

    @PutMapping("/cards/{id}")
    public ResponseEntity<Map<String, String>> updateCard(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader("X-Manager-Key") String managerKey) {
        String commandId = kafkaService.updateCard(id, body, managerKey);
        return ResponseEntity.accepted().body(Map.of(
                "status", "COMMAND_SENT", "commandId", commandId));
    }

    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Map<String, String>> deleteCard(
            @PathVariable Long id,
            @RequestHeader("X-Manager-Key") String managerKey) {
        String commandId = kafkaService.deleteCard(id, managerKey);
        return ResponseEntity.accepted().body(Map.of(
                "status", "COMMAND_SENT", "commandId", commandId));
    }
}