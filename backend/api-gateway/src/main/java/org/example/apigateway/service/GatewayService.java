package org.example.apigateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GatewayService {

    private static final Logger log = LoggerFactory.getLogger(GatewayService.class);
    private final WebClient webClient;

    @Value("${services.atm.url}") private String atmServiceUrl;
    @Value("${services.branch.url}") private String branchServiceUrl;
    @Value("${services.order.url}") private String orderServiceUrl;
    @Value("${services.card.url}") private String cardServiceUrl;
    @Value("${services.client.url}") private String clientServiceUrl;
    @Value("${services.account.url}") private String accountServiceUrl;
    @Value("${manager.secret.key}") private String managerSecretKey;

    public GatewayService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * GET запрос с проксированием всех заголовков
     */
    public String proxyGet(String url, HttpHeaders originalHeaders) {
        return webClient.get()
                .uri(url)
                .headers(h -> copyHeaders(originalHeaders, h))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.error("Proxy GET error for URL: {}", url, e);
                    return reactor.core.publisher.Mono.just(
                            "{\"error\":\"Service unavailable\"}");
                })
                .block();
    }

    /**
     * GET без заголовков (для внутренних вызовов)
     */
    public String proxyGet(String url) {
        return proxyGet(url, new HttpHeaders());
    }

    /**
     * POST запрос с телом и проксированием заголовков
     */
    public String proxyPost(String url, Object body, HttpHeaders originalHeaders) {
        return webClient.post()
                .uri(url)
                .headers(h -> copyHeaders(originalHeaders, h))
                .bodyValue(body != null ? body : Map.of())
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.error("Proxy POST error for URL: {}", url, e);
                    return reactor.core.publisher.Mono.just(
                            "{\"error\":\"Service unavailable\"}");
                })
                .block();
    }

    /**
     * PUT запрос с телом и проксированием заголовков
     */
    public String proxyPut(String url, Object body, HttpHeaders originalHeaders) {
        return webClient.put()
                .uri(url)
                .headers(h -> copyHeaders(originalHeaders, h))
                .bodyValue(body != null ? body : Map.of())
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.error("Proxy PUT error for URL: {}", url, e);
                    return reactor.core.publisher.Mono.just(
                            "{\"error\":\"Service unavailable\"}");
                })
                .block();
    }

    /**
     * DELETE запрос с проксированием заголовков
     */
    public String proxyDelete(String url, HttpHeaders originalHeaders) {
        return webClient.delete()
                .uri(url)
                .headers(h -> copyHeaders(originalHeaders, h))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.error("Proxy DELETE error for URL: {}", url, e);
                    return reactor.core.publisher.Mono.just(
                            "{\"error\":\"Service unavailable\"}");
                })
                .block();
    }

    /**
     * Копирование заголовков из запроса в прокси-запрос
     * Пропускаем системные заголовки
     */
    private void copyHeaders(HttpHeaders source, HttpHeaders target) {
        List<String> skipHeaders = List.of(
                "host", "content-length", "transfer-encoding", "connection"
        );

        source.forEach((key, values) -> {
            if (!skipHeaders.contains(key.toLowerCase())) {
                values.forEach(value -> target.add(key, value));
            }
        });
    }

    // Геттеры
    public String getAtmServiceUrl() { return atmServiceUrl; }
    public String getBranchServiceUrl() { return branchServiceUrl; }
    public String getOrderServiceUrl() { return orderServiceUrl; }
    public String getCardServiceUrl() { return cardServiceUrl; }
    public String getClientServiceUrl() { return clientServiceUrl; }
    public String getAccountServiceUrl() { return accountServiceUrl; }
    public String getManagerSecretKey() { return managerSecretKey; }
}