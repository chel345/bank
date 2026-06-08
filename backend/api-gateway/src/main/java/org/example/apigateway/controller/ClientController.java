package org.example.apigateway.controller;

import org.example.apigateway.service.GatewayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ClientController {

    private final GatewayService gatewayService;

    public ClientController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    /**
     * Извлечение заголовков из HttpServletRequest
     */
    private HttpHeaders getHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.addAll(name, Collections.list(request.getHeaders(name)));
        }
        return headers;
    }

    @GetMapping("/profile")
    public ResponseEntity<String> getMyProfile(HttpServletRequest request) {
        Long clientId = (Long) request.getAttribute("clientId");
        return ResponseEntity.ok(gatewayService.proxyGet(
                gatewayService.getClientServiceUrl() + "/api/clients/" + clientId,
                getHeaders(request)));
    }

    @PutMapping("/profile")
    public ResponseEntity<String> updateMyProfile(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        Long clientId = (Long) request.getAttribute("clientId");
        return ResponseEntity.ok(gatewayService.proxyPut(
                gatewayService.getClientServiceUrl() + "/api/clients/" + clientId,
                body,
                getHeaders(request)));
    }

    @PutMapping("/clients/{id}")
    public ResponseEntity<String> updateClientByManager(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        return ResponseEntity.ok(gatewayService.proxyPut(
                gatewayService.getClientServiceUrl() + "/api/clients/" + id,
                body,
                getHeaders(request)));
    }

    @DeleteMapping("/clients/{id}")
    public ResponseEntity<String> deleteClient(
            @PathVariable Long id,
            HttpServletRequest request) {

        return ResponseEntity.ok(gatewayService.proxyDelete(
                gatewayService.getClientServiceUrl() + "/api/clients/" + id,
                getHeaders(request)));
    }

    @GetMapping("/legal-entities")
    public ResponseEntity<String> getMyLegalEntities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Long clientId = (Long) request.getAttribute("clientId");
        String url = String.format("%s/api/legal-entities/by-client/%d?page=%d&size=%d",
                gatewayService.getClientServiceUrl(), clientId, page, size);
        return ResponseEntity.ok(gatewayService.proxyGet(url, getHeaders(request)));
    }

    @GetMapping("/legal-entities/{id}")
    public ResponseEntity<String> getLegalEntityById(
            @PathVariable Long id,
            HttpServletRequest request) {

        return ResponseEntity.ok(gatewayService.proxyGet(
                gatewayService.getClientServiceUrl() + "/api/legal-entities/" + id,
                getHeaders(request)));
    }

    @PostMapping("/legal-entities")
    public ResponseEntity<String> createLegalEntity(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        Long clientId = (Long) request.getAttribute("clientId");
        body.put("clientId", clientId);
        return ResponseEntity.ok(gatewayService.proxyPost(
                gatewayService.getClientServiceUrl() + "/api/legal-entities",
                body,
                getHeaders(request)));
    }

    @PutMapping("/legal-entities/{id}")
    public ResponseEntity<String> updateLegalEntity(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        return ResponseEntity.ok(gatewayService.proxyPut(
                gatewayService.getClientServiceUrl() + "/api/legal-entities/" + id,
                body,
                getHeaders(request)));
    }

    @DeleteMapping("/legal-entities/{id}")
    public ResponseEntity<String> deleteLegalEntity(
            @PathVariable Long id,
            HttpServletRequest request) {
        return ResponseEntity.ok(gatewayService.proxyDelete(
                gatewayService.getClientServiceUrl() + "/api/legal-entities/" + id,
                getHeaders(request)));
    }
}