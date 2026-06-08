package org.example.orderservice.kafka;

import org.example.orderservice.model.Order;
import org.example.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class OrderCommandConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCommandConsumer.class);
    private final OrderRepository repository;
    private final ObjectMapper objectMapper;

    @Value("${manager.secret.key}")
    private String managerSecretKey;

    public OrderCommandConsumer(OrderRepository repository) {
        this.repository = repository;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "order-commands", groupId = "order-service")
    @Transactional
    public void handleCommand(String message) {
        log.info("Received order command: {}", message);

        try {
            JsonNode cmd = objectMapper.readTree(message);
            String type = cmd.get("type").asText();
            String key = cmd.get("managerKey").asText();

            if (!managerSecretKey.equals(key)) {
                log.error("Invalid manager key for command: {}", type);
                return;
            }

            switch (type) {
                case "CREATE_ORDER" -> {
                    Order order = new Order();
                    order.setCustomerId(cmd.get("customerId").asLong());
                    order.setDate(LocalDate.parse(cmd.get("date").asText()));
                    order.setBranchId(cmd.get("branchId").asLong());

                    Order saved = repository.save(order);
                    log.info("Order created with ID: {}", saved.getId());
                }

                case "UPDATE_ORDER" -> {
                    Long id = cmd.get("id").asLong();
                    repository.findById(id).ifPresent(order -> {
                        if (cmd.has("customerId")) order.setCustomerId(cmd.get("customerId").asLong());
                        if (cmd.has("date")) order.setDate(LocalDate.parse(cmd.get("date").asText()));
                        if (cmd.has("branchId")) order.setBranchId(cmd.get("branchId").asLong());
                        repository.save(order);
                        log.info("Order updated: {}", id);
                    });
                }

                case "DELETE_ORDER" -> {
                    Long id = cmd.get("id").asLong();
                    repository.deleteById(id);
                    log.info("Order deleted: {}", id);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process order command", e);
        }
    }
}