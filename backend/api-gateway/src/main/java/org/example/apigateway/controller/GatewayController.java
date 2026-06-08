package org.example.apigateway.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
public class GatewayController {

    private final WebClient clientServiceClient;
    private final WebClient accountServiceClient;
    private final WebClient atmServiceClient;
    private final WebClient branchServiceClient;
    private final WebClient orderServiceClient;
    private final WebClient cardServiceClient;

    public GatewayController(@Qualifier("clientServiceClient") WebClient clientServiceClient,
                             @Qualifier("accountServiceClient") WebClient accountServiceClient,
                             @Qualifier("atmServiceClient") WebClient atmServiceClient,
                             @Qualifier("branchServiceClient") WebClient branchServiceClient,
                             @Qualifier("orderServiceClient") WebClient orderServiceClient,
                             @Qualifier("cardServiceClient") WebClient cardServiceClient) {
        this.clientServiceClient = clientServiceClient;
        this.accountServiceClient = accountServiceClient;
        this.atmServiceClient = atmServiceClient;
        this.branchServiceClient = branchServiceClient;
        this.orderServiceClient = orderServiceClient;
        this.cardServiceClient = cardServiceClient;
    }

    // ===== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =====

    private String removePasswordHash(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);
            if (node.isObject() && node.has("passwordHash")) {
                ((ObjectNode) node).remove("passwordHash");
            }
            return mapper.writeValueAsString(node);
        } catch (Exception e) {
            return json;
        }
    }

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    // ===== CLIENT SERVICE =====

    // Создание клиента (только менеджер)
    @PostMapping("/clients")
    public Mono<ResponseEntity<String>> createClient(
            @RequestBody String body,
            @RequestHeader("X-Manager-Key") String managerKey) {
        return clientServiceClient.post()
                .uri("/api/clients")
                .header("X-Manager-Key", managerKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .toEntity(String.class);
    }

    // Получение СВОЕГО профиля (id из JWT)
    @GetMapping("/clients/me")
    public Mono<ResponseEntity<String>> getCurrentClient(HttpServletRequest request) {
        Long userId = getUserId(request);
        return clientServiceClient.get().uri("/api/clients/{id}", userId)
                .retrieve()
                .toEntity(String.class)
                .map(entity -> new ResponseEntity<>(
                        removePasswordHash(entity.getBody()),
                        entity.getHeaders(),
                        entity.getStatusCode()
                ));
    }

    // Получение клиента по ID (для менеджера/админа)
    @GetMapping("/clients/{id}")
    public Mono<ResponseEntity<String>> getClient(@PathVariable Long id) {
        return clientServiceClient.get().uri("/api/clients/{id}", id)
                .retrieve()
                .toEntity(String.class)
                .map(entity -> new ResponseEntity<>(
                        removePasswordHash(entity.getBody()),
                        entity.getHeaders(),
                        entity.getStatusCode()
                ));
    }

    // Получение клиента по телефону
    @GetMapping("/clients/by-phone/{phone}")
    public Mono<ResponseEntity<String>> getClientByPhone(@PathVariable String phone) {
        return clientServiceClient.get().uri("/api/clients/by-phone/{phone}", phone)
                .retrieve()
                .toEntity(String.class)
                .map(entity -> new ResponseEntity<>(
                        removePasswordHash(entity.getBody()),
                        entity.getHeaders(),
                        entity.getStatusCode()
                ));
    }

    // Обновление СВОЕГО профиля (id из JWT)
    @PutMapping("/clients/me")
    public Mono<ResponseEntity<String>> updateCurrentClient(
            @RequestBody String body,
            @RequestHeader(value = "X-Manager-Key", required = false) String managerKey,
            HttpServletRequest request) {

        Long userId = getUserId(request);
        return updateClientById(userId, body, managerKey, userId);
    }

    // Обновление клиента по ID (менеджер)
    @PutMapping("/clients/{id}")
    public Mono<ResponseEntity<String>> updateClient(
            @PathVariable Long id,
            @RequestBody String body,
            @RequestHeader(value = "X-Manager-Key", required = false) String managerKey,
            HttpServletRequest request) {

        Long userId = getUserId(request);
        return updateClientById(id, body, managerKey, userId);
    }

    // Внутренний метод обновления
    private Mono<ResponseEntity<String>> updateClientById(
            Long id, String body, String managerKey, Long userId) {

        var request = clientServiceClient.put()
                .uri("/api/clients/{id}", id)
                .header("Content-Type", "application/json")
                .bodyValue(body);

        if (managerKey != null) request.header("X-Manager-Key", managerKey);
        if (userId != null) request.header("X-Client-Id", userId.toString());

        return request.retrieve().toEntity(String.class)
                .map(entity -> new ResponseEntity<>(
                        removePasswordHash(entity.getBody()),
                        entity.getHeaders(),
                        entity.getStatusCode()
                ));
    }

    // Удаление клиента (только менеджер)
    @DeleteMapping("/clients/{id}")
    public Mono<ResponseEntity<String>> deleteClient(
            @PathVariable Long id,
            @RequestHeader("X-Manager-Key") String managerKey) {
        return clientServiceClient.delete()
                .uri("/api/clients/{id}", id)
                .header("X-Manager-Key", managerKey)
                .retrieve()
                .toEntity(String.class);
    }

    // Создание юрлица (только менеджер)
    @PostMapping("/legal-entities")
    public Mono<ResponseEntity<String>> createLegalEntity(
            @RequestBody String body,
            @RequestHeader("X-Manager-Key") String managerKey) {
        return clientServiceClient.post()
                .uri("/api/legal-entities")
                .header("X-Manager-Key", managerKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .toEntity(String.class);
    }

    // Получение юрлиц ТЕКУЩЕГО клиента (id из JWT)
    @GetMapping("/legal-entities/me")
    public Mono<ResponseEntity<String>> getMyLegalEntities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        return clientServiceClient.get()
                .uri("/api/legal-entities/by-client/{clientId}?page={page}&size={size}", userId, page, size)
                .retrieve().toEntity(String.class);
    }

    // Получение юрлиц клиента по ID
    @GetMapping("/legal-entities/by-client/{clientId}")
    public Mono<ResponseEntity<String>> getLegalEntitiesByClient(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return clientServiceClient.get()
                .uri("/api/legal-entities/by-client/{clientId}?page={page}&size={size}", clientId, page, size)
                .retrieve().toEntity(String.class);
    }

    // Получение юрлица по ID
    @GetMapping("/legal-entities/{id}")
    public Mono<ResponseEntity<String>> getLegalEntity(@PathVariable Long id) {
        return clientServiceClient.get().uri("/api/legal-entities/{id}", id)
                .retrieve().toEntity(String.class);
    }

    // Обновление юрлица (пользователь может только свои)
    @PutMapping("/legal-entities/{id}")
    public Mono<ResponseEntity<String>> updateLegalEntity(
            @PathVariable Long id,
            @RequestBody String body,
            @RequestHeader(value = "X-Manager-Key", required = false) String managerKey,
            HttpServletRequest request) {

        Long userId = getUserId(request);
        var req = clientServiceClient.put()
                .uri("/api/legal-entities/{id}", id)
                .header("Content-Type", "application/json")
                .bodyValue(body);

        if (managerKey != null) req.header("X-Manager-Key", managerKey);
        if (userId != null) req.header("X-Client-Id", userId.toString());

        return req.retrieve().toEntity(String.class);
    }

    // Удаление юрлица (только менеджер)
    @DeleteMapping("/legal-entities/{id}")
    public Mono<ResponseEntity<String>> deleteLegalEntity(
            @PathVariable Long id,
            @RequestHeader("X-Manager-Key") String managerKey) {
        return clientServiceClient.delete()
                .uri("/api/legal-entities/{id}", id)
                .header("X-Manager-Key", managerKey)
                .retrieve()
                .toEntity(String.class);
    }

    // Связывание клиента с юрлицом (только менеджер)
    @PostMapping("/legal-entities/link")
    public Mono<ResponseEntity<String>> linkClientToLegalEntity(
            @RequestParam Long clientId,
            @RequestParam Long legalEntityId,
            @RequestHeader("X-Manager-Key") String managerKey) {
        return clientServiceClient.post()
                .uri("/api/legal-entities/link?clientId={clientId}&legalEntityId={legalEntityId}",
                        clientId, legalEntityId)
                .header("X-Manager-Key", managerKey)
                .retrieve()
                .toEntity(String.class);
    }

    // Разрыв связи клиент-юрлицо (только менеджер)
    @DeleteMapping("/legal-entities/link")
    public Mono<ResponseEntity<String>> unlinkClientFromLegalEntity(
            @RequestParam Long clientId,
            @RequestParam Long legalEntityId,
            @RequestHeader("X-Manager-Key") String managerKey) {
        return clientServiceClient.delete()
                .uri("/api/legal-entities/link?clientId={clientId}&legalEntityId={legalEntityId}",
                        clientId, legalEntityId)
                .header("X-Manager-Key", managerKey)
                .retrieve()
                .toEntity(String.class);
    }

    // ===== ACCOUNT SERVICE =====

    // Создание личного счёта (только менеджер)
    @PostMapping("/accounts/personal")
    public Mono<ResponseEntity<String>> createPersonalAccount(
            @RequestParam Long ownerId,
            @RequestParam(defaultValue = "RUB") String currency,
            @RequestHeader("X-Manager-Key") String managerKey) {
        return accountServiceClient.post()
                .uri("/api/accounts/personal?ownerId={ownerId}&currency={currency}", ownerId, currency)
                .header("X-Manager-Key", managerKey)
                .retrieve()
                .toEntity(String.class);
    }

    // Создание счёта юрлица (только менеджер)
    @PostMapping("/accounts/business")
    public Mono<ResponseEntity<String>> createBusinessAccount(
            @RequestParam Long ownerId,
            @RequestParam(defaultValue = "RUB") String currency,
            @RequestHeader("X-Manager-Key") String managerKey) {
        return accountServiceClient.post()
                .uri("/api/accounts/business?ownerId={ownerId}&currency={currency}", ownerId, currency)
                .header("X-Manager-Key", managerKey)
                .retrieve()
                .toEntity(String.class);
    }

    // Получение СВОИХ личных счетов (id из JWT)
    @GetMapping("/accounts/me")
    public Mono<ResponseEntity<String>> getMyAccounts(HttpServletRequest request) {
        Long userId = getUserId(request);
        return accountServiceClient.get().uri("/api/accounts/personal/owner/{ownerId}", userId)
                .retrieve().toEntity(String.class);
    }

    // Получение личных счетов владельца
    @GetMapping("/accounts/personal/owner/{ownerId}")
    public Mono<ResponseEntity<String>> getPersonalAccounts(@PathVariable Long ownerId) {
        return accountServiceClient.get().uri("/api/accounts/personal/owner/{ownerId}", ownerId)
                .retrieve().toEntity(String.class);
    }

    // Получение счетов юрлица
    @GetMapping("/accounts/business/owner/{ownerId}")
    public Mono<ResponseEntity<String>> getBusinessAccounts(@PathVariable Long ownerId) {
        return accountServiceClient.get().uri("/api/accounts/business/owner/{ownerId}", ownerId)
                .retrieve().toEntity(String.class);
    }

    // Закрытие личного счёта (только менеджер)
    @DeleteMapping("/accounts/personal/{id}")
    public Mono<ResponseEntity<String>> closePersonalAccount(
            @PathVariable Long id,
            @RequestHeader("X-Manager-Key") String managerKey) {
        return accountServiceClient.delete()
                .uri("/api/accounts/personal/{id}", id)
                .header("X-Manager-Key", managerKey)
                .retrieve()
                .toEntity(String.class);
    }

    // Закрытие счёта юрлица (только менеджер)
    @DeleteMapping("/accounts/business/{id}")
    public Mono<ResponseEntity<String>> closeBusinessAccount(
            @PathVariable Long id,
            @RequestHeader("X-Manager-Key") String managerKey) {
        return accountServiceClient.delete()
                .uri("/api/accounts/business/{id}", id)
                .header("X-Manager-Key", managerKey)
                .retrieve()
                .toEntity(String.class);
    }

    // Зачисление на личный счёт
    @PostMapping("/accounts/personal/{id}/deposit")
    public Mono<ResponseEntity<String>> depositToPersonalAccount(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestHeader("X-Manager-Key") String managerKey) {

        // Правильный URI: /api/accounts/personal/{id}/deposit (account-service уже имеет /api/accounts)
        return accountServiceClient.post()
                .uri("/api/accounts/personal/{id}/deposit?amount={amount}", id, amount)
                .header("X-Manager-Key", managerKey)
                .retrieve()
                .toEntity(String.class)
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}")
                ));
    }

    // ===== ATM SERVICE =====

    @GetMapping("/atms")
    public Mono<ResponseEntity<String>> getAtms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return atmServiceClient.get().uri("/api/atms?page={page}&size={size}", page, size)
                .retrieve().toEntity(String.class);
    }

    @GetMapping("/atms/{id}")
    public Mono<ResponseEntity<String>> getAtm(@PathVariable Long id) {
        return atmServiceClient.get().uri("/api/atms/{id}", id)
                .retrieve().toEntity(String.class);
    }

    @GetMapping("/atms/city/{city}")
    public Mono<ResponseEntity<String>> getAtmsByCity(
            @PathVariable String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return atmServiceClient.get().uri("/api/atms/city/{city}?page={page}&size={size}", city, page, size)
                .retrieve().toEntity(String.class);
    }

    // ===== BRANCH SERVICE =====

    @GetMapping("/branches/city/{city}")
    public Mono<ResponseEntity<String>> getBranchesByCity(
            @PathVariable String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return branchServiceClient.get()
                .uri("/api/branches/city/{city}?page={page}&size={size}", city, page, size)
                .retrieve().toEntity(String.class);
    }

    @GetMapping("/branches/{id}")
    public Mono<ResponseEntity<String>> getBranch(@PathVariable Long id) {
        return branchServiceClient.get().uri("/api/branches/{id}", id)
                .retrieve().toEntity(String.class);
    }

    // ===== ORDER SERVICE =====

    // Получение СВОИХ заказов (id из JWT)
    @GetMapping("/orders/me")
    public Mono<ResponseEntity<String>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        return orderServiceClient.get()
                .uri("/api/orders/customer/{customerId}?page={page}&size={size}", userId, page, size)
                .retrieve().toEntity(String.class);
    }

    @GetMapping("/orders/customer/{customerId}")
    public Mono<ResponseEntity<String>> getOrdersByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return orderServiceClient.get()
                .uri("/api/orders/customer/{customerId}?page={page}&size={size}", customerId, page, size)
                .retrieve().toEntity(String.class);
    }

    @GetMapping("/orders/{id}")
    public Mono<ResponseEntity<String>> getOrder(@PathVariable Long id) {
        return orderServiceClient.get().uri("/api/orders/{id}", id)
                .retrieve().toEntity(String.class);
    }

    // ===== CARD SERVICE =====

    // Получение карт СВОЕГО счёта (счёт привязан к id из JWT)
    @GetMapping("/cards/by-account/{accountId}")
    public Mono<ResponseEntity<String>> getCardsByAccount(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return cardServiceClient.get()
                .uri("/api/cards/account/{accountId}?page={page}&size={size}", accountId, page, size)
                .retrieve().toEntity(String.class);
    }

    @GetMapping("/cards/{id}")
    public Mono<ResponseEntity<String>> getCard(@PathVariable Long id) {
        return cardServiceClient.get().uri("/api/cards/{id}", id)
                .retrieve().toEntity(String.class);
    }
}