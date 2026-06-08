package org.example.apigateway.service;

import org.example.apigateway.config.JwtUtil;
import org.example.apigateway.dto.JwtResponse;
import org.example.apigateway.dto.LoginRequest;
import org.example.apigateway.dto.RegisterRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    private final WebClient clientServiceClient;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Value("${manager.secret.key}")
    private String managerSecretKey;

    public AuthService(@Qualifier("clientServiceClient") WebClient clientServiceClient,
                       JwtUtil jwtUtil,
                       PasswordEncoder passwordEncoder) {
        this.clientServiceClient = clientServiceClient;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = new ObjectMapper();
    }

    public Mono<JwtResponse> login(LoginRequest request) {
        return clientServiceClient.get()
                .uri("/api/clients/by-phone/{phone}", request.getPhone())
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        response -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный телефон или пароль")))
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        JsonNode client = objectMapper.readTree(response);
                        String passwordHash = client.get("passwordHash").asText();

                        if (passwordEncoder.matches(request.getPassword(), passwordHash)) {
                            Long userId = client.get("id").asLong();
                            String token = jwtUtil.generateToken(userId);
                            return new JwtResponse(token, userId);
                        } else {
                            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный телефон или пароль");
                        }
                    } catch (ResponseStatusException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ошибка аутентификации");
                    }
                });
    }

    public Mono<JwtResponse> register(RegisterRequest request, String managerKey) {
        if (!managerSecretKey.equals(managerKey)) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Только менеджер может регистрировать клиентов"));
        }

        return clientServiceClient.post()
                .uri("/api/clients")
                .header("X-Manager-Key", managerKey)
                .header("Content-Type", "application/json")
                .bodyValue(String.format(
                        "{\"email\":\"%s\",\"password\":\"%s\",\"phone\":\"%s\",\"passportData\":\"%s\"}",
                        request.getEmail(), request.getPassword(), request.getPhone(),
                        request.getPassportData() != null ? request.getPassportData() : ""
                ))
                .retrieve()
                .onStatus(status -> status.isError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, body))))
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        JsonNode client = objectMapper.readTree(response);
                        Long userId = client.get("id").asLong();
                        String token = jwtUtil.generateToken(userId);
                        return new JwtResponse(token, userId);
                    } catch (Exception e) {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка создания клиента");
                    }
                });
    }
}