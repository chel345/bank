package org.example.apigateway.service;

import org.example.apigateway.dto.LoginRequest;
import org.example.apigateway.dto.RegisterRequest;
import org.example.apigateway.dto.TokenResponse;
import org.example.apigateway.security.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final WebClient webClient;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Value("${services.client.url}")
    private String clientServiceUrl;

    public AuthService(WebClient webClient, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.webClient = webClient;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = new ObjectMapper();
    }

    public TokenResponse login(LoginRequest request) {
        try {
            String response = webClient.get()
                    .uri(clientServiceUrl + "/api/clients/by-phone/{phone}", request.getPhone())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode client = objectMapper.readTree(response);
            Long clientId = client.get("id").asLong();
            String passwordHash = client.get("passwordHash").asText();

            if (!passwordEncoder.matches(request.getPassword(), passwordHash)) {
                throw new RuntimeException("Invalid password");
            }

            String token = jwtUtil.generateToken(clientId, "CLIENT");
            log.info("Client {} logged in", clientId);
            return new TokenResponse(token, clientId, "CLIENT");
        } catch (Exception e) {
            log.error("Login failed", e);
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    public TokenResponse register(RegisterRequest request) {
        try {
            Map<String, Object> body = Map.of(
                    "email", request.getEmail(),
                    "password", request.getPassword(),
                    "phone", request.getPhone(),
                    "passportData", request.getPassportData() != null ? request.getPassportData() : ""
            );

            String response = webClient.post()
                    .uri(clientServiceUrl + "/api/clients")
                    .header("Content-Type", "application/json")
                    .header("X-Manager-Key", request.getManagerKey())
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode client = objectMapper.readTree(response);
            Long clientId = client.get("id").asLong();
            String token = jwtUtil.generateToken(clientId, "CLIENT");
            log.info("Client {} registered", clientId);
            return new TokenResponse(token, clientId, "CLIENT");
        } catch (Exception e) {
            log.error("Registration failed", e);
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }
}