package org.example.apigateway.controller;

import org.example.apigateway.dto.ErrorResponse;
import org.example.apigateway.dto.TransactionRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final WebClient clientServiceClient;
    private final WebClient accountServiceClient;

    public TransactionController(@Qualifier("clientServiceClient") WebClient clientServiceClient,
                                 @Qualifier("accountServiceClient") WebClient accountServiceClient) {
        this.clientServiceClient = clientServiceClient;
        this.accountServiceClient = accountServiceClient;
    }

    @PostMapping
    public Mono<ResponseEntity<?>> createTransaction(
            @RequestBody TransactionRequest request,
            @RequestHeader("X-Manager-Key") String managerKey,
            @RequestAttribute(value = "userId", required = false) Long userId) {

        // Проверка связи клиента с юрлицом, если указан legalEntityId
        Mono<Boolean> validationMono;
        if (request.getLegalEntityId() != null && userId != null) {
            validationMono = clientServiceClient.get()
                    .uri("/api/legal-entities/by-client/{clientId}", userId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(response -> {
                        // Проверяем, что legalEntityId есть в списке юрлиц клиента
                        if (!response.contains(request.getLegalEntityId().toString())) {
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                    "Клиент не связан с указанным юридическим лицом");
                        }
                        return true;
                    });
        } else {
            validationMono = Mono.just(true);
        }

        return validationMono.flatMap(valid -> {
            String transferPath = "personal".equals(request.getAccountType()) ?
                    "/api/accounts/transfer/personal" : "/api/accounts/transfer/business";

            return accountServiceClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(transferPath)
                            .queryParam("from", request.getFromAccountId())
                            .queryParam("to", request.getToAccountId())
                            .queryParam("amount", request.getAmount())
                            .build())
                    .header("X-Manager-Key", managerKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(ResponseEntity::ok);
        });
    }
}