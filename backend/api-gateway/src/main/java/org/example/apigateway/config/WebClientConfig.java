package org.example.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.client.url}")
    private String clientServiceUrl;

    @Value("${services.account.url}")
    private String accountServiceUrl;

    @Value("${services.atm.url}")
    private String atmServiceUrl;

    @Value("${services.branch.url}")
    private String branchServiceUrl;

    @Value("${services.order.url}")
    private String orderServiceUrl;

    @Value("${services.card.url}")
    private String cardServiceUrl;

    @Bean
    public WebClient clientServiceClient() {
        return WebClient.create(clientServiceUrl);
    }

    @Bean
    public WebClient accountServiceClient() {
        return WebClient.create(accountServiceUrl);
    }

    @Bean
    public WebClient atmServiceClient() {
        return WebClient.create(atmServiceUrl);
    }

    @Bean
    public WebClient branchServiceClient() {
        return WebClient.create(branchServiceUrl);
    }

    @Bean
    public WebClient orderServiceClient() {
        return WebClient.create(orderServiceUrl);
    }

    @Bean
    public WebClient cardServiceClient() {
        return WebClient.create(cardServiceUrl);
    }
}