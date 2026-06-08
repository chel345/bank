package org.example.apigateway.controller;

import org.example.apigateway.service.GatewayService;
import org.example.apigateway.service.KafkaCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class BranchController {

    private final GatewayService gatewayService;
    private final KafkaCommandService kafkaService;

    public BranchController(GatewayService gatewayService, KafkaCommandService kafkaService) {
        this.gatewayService = gatewayService;
        this.kafkaService = kafkaService;
    }

    @GetMapping("/branches/city/{city}")
    public ResponseEntity<String> getBranchesByCity(
            @PathVariable String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String url = String.format("%s/api/branches/city/%s?page=%d&size=%d",
                gatewayService.getBranchServiceUrl(), city, page, size);
        return ResponseEntity.ok(gatewayService.proxyGet(url));
    }

    @GetMapping("/branches/{id}")
    public ResponseEntity<String> getBranchById(@PathVariable Long id) {
        return ResponseEntity.ok(gatewayService.proxyGet(
                gatewayService.getBranchServiceUrl() + "/api/branches/" + id));
    }

    @PostMapping("/branches")
    public ResponseEntity<Map<String, String>> createBranch(
            @RequestBody Map<String, String> body,
            @RequestHeader("X-Manager-Key") String managerKey) {
        String commandId = kafkaService.createBranch(
                body.get("address"), body.get("city"), body.get("phone"),
                body.get("email"), body.get("workingHours"), managerKey);
        return ResponseEntity.accepted().body(Map.of(
                "status", "COMMAND_SENT", "commandId", commandId));
    }

    @PutMapping("/branches/{id}")
    public ResponseEntity<Map<String, String>> updateBranch(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader("X-Manager-Key") String managerKey) {
        String commandId = kafkaService.updateBranch(id, body, managerKey);
        return ResponseEntity.accepted().body(Map.of(
                "status", "COMMAND_SENT", "commandId", commandId));
    }

    @DeleteMapping("/branches/{id}")
    public ResponseEntity<Map<String, String>> deleteBranch(
            @PathVariable Long id,
            @RequestHeader("X-Manager-Key") String managerKey) {
        String commandId = kafkaService.deleteBranch(id, managerKey);
        return ResponseEntity.accepted().body(Map.of(
                "status", "COMMAND_SENT", "commandId", commandId));
    }
}