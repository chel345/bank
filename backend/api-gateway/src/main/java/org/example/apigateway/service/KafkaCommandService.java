package org.example.apigateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class KafkaCommandService {

    private static final Logger log = LoggerFactory.getLogger(KafkaCommandService.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaCommandService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    private String sendCommand(String topic, Map<String, Object> command) {
        try {
            String correlationId = UUID.randomUUID().toString();
            command.put("commandId", correlationId);
            command.put("timestamp", Instant.now().toString());

            String message = objectMapper.writeValueAsString(command);

            kafkaTemplate.send(topic, correlationId, message)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Command sent to {}: {}", topic, correlationId);
                        } else {
                            log.error("Failed to send command to {}", topic, ex);
                        }
                    });

            return correlationId;
        } catch (Exception e) {
            throw new RuntimeException("Failed to send command", e);
        }
    }

    // ===== ATM Commands =====
    public String createAtm(String address, String city) {
        return sendCommand("atm-events", Map.of(
                "eventType", "ATM_CREATED",
                "address", address,
                "city", city
        ));
    }

    public String updateAtm(Long id, String address, String city) {
        Map<String, Object> cmd = new HashMap<>();
        cmd.put("eventType", "ATM_UPDATED");
        cmd.put("id", id);
        if (address != null) cmd.put("address", address);
        if (city != null) cmd.put("city", city);
        return sendCommand("atm-events", cmd);
    }

    public String deleteAtm(Long id) {
        return sendCommand("atm-events", Map.of(
                "eventType", "ATM_DELETED",
                "id", id
        ));
    }

    // ===== Branch Commands =====
    public String createBranch(String address, String city, String phone,
                               String email, String workingHours, String managerKey) {
        Map<String, Object> cmd = new HashMap<>();
        cmd.put("type", "CREATE_BRANCH");
        cmd.put("managerKey", managerKey);
        cmd.put("address", address);
        cmd.put("city", city);
        cmd.put("phone", phone);
        if (email != null) cmd.put("email", email);
        if (workingHours != null) cmd.put("workingHours", workingHours);
        return sendCommand("branch-commands", cmd);
    }

    public String updateBranch(Long id, Map<String, Object> fields, String managerKey) {
        fields.put("type", "UPDATE_BRANCH");
        fields.put("id", id);
        fields.put("managerKey", managerKey);
        return sendCommand("branch-commands", fields);
    }

    public String deleteBranch(Long id, String managerKey) {
        return sendCommand("branch-commands", Map.of(
                "type", "DELETE_BRANCH",
                "id", id,
                "managerKey", managerKey
        ));
    }

    // ===== Order Commands =====
    public String createOrder(Long customerId, String date, Long branchId, String managerKey) {
        return sendCommand("order-commands", Map.of(
                "type", "CREATE_ORDER",
                "managerKey", managerKey,
                "customerId", customerId,
                "date", date,
                "branchId", branchId
        ));
    }

    public String updateOrder(Long id, Map<String, Object> fields, String managerKey) {
        fields.put("type", "UPDATE_ORDER");
        fields.put("id", id);
        fields.put("managerKey", managerKey);
        return sendCommand("order-commands", fields);
    }

    public String deleteOrder(Long id, String managerKey) {
        return sendCommand("order-commands", Map.of(
                "type", "DELETE_ORDER",
                "id", id,
                "managerKey", managerKey
        ));
    }

    // ===== Card Commands =====
    public String createCard(Long accountId, String cardNumber, String managerKey) {
        return sendCommand("card-commands", Map.of(
                "type", "CREATE_CARD",
                "managerKey", managerKey,
                "accountId", accountId,
                "cardNumber", cardNumber
        ));
    }

    public String updateCard(Long id, Map<String, Object> fields, String managerKey) {
        fields.put("type", "UPDATE_CARD");
        fields.put("id", id);
        fields.put("managerKey", managerKey);
        return sendCommand("card-commands", fields);
    }

    public String deleteCard(Long id, String managerKey) {
        return sendCommand("card-commands", Map.of(
                "type", "DELETE_CARD",
                "id", id,
                "managerKey", managerKey
        ));
    }
}