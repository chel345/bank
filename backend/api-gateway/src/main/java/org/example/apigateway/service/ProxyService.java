package org.example.apigateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
public class ProxyService {

    private final WebClient clientServiceClient;
    private final ObjectMapper objectMapper;

    public ProxyService(@Qualifier("clientServiceClient") WebClient clientServiceClient) {
        this.clientServiceClient = clientServiceClient;
        this.objectMapper = new ObjectMapper();
    }

    // Проверка связи клиента с юрлицом
    public Mono<Boolean> isClientLinkedToLegalEntity(Long clientId, Long legalEntityId) {
        return clientServiceClient.get()
                .uri("/api/clients/{id}", clientId)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        // Проверяем, что клиент существует
                        JsonNode client = objectMapper.readTree(response);
                        return true;
                    } catch (Exception e) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Клиент не найден");
                    }
                });
    }

    // Прокси с передачей заголовков
    public WebClient.RequestHeadersSpec<?> proxyRequest(WebClient client, String uri,
                                                        String managerKey, Long userId) {
        var request = client.get()
                .uri(uri);

        if (managerKey != null) {
            request.header("X-Manager-Key", managerKey);
        }
        if (userId != null) {
            request.header("X-Client-Id", userId.toString());
        }

        return request;
    }
}