// kafka/AtmEventConsumer.java
package com.example.atm.kafka;

import com.example.atm.model.Atm;
import com.example.atm.repository.AtmRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AtmEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AtmEventConsumer.class);
    private final AtmRepository atmRepository;
    private final ObjectMapper objectMapper;

    public AtmEventConsumer(AtmRepository atmRepository) {
        this.atmRepository = atmRepository;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "atm-events", groupId = "atm-service-group")
    @Transactional
    public void handleAtmEvent(String message) {
        log.info("=== RECEIVED MESSAGE: {} ===", message);

        try {
            JsonNode event = objectMapper.readTree(message);

            // Проверяем, есть ли поле eventType
            if (!event.has("eventType")) {
                log.warn("Message doesn't have eventType field: {}", message);
                return;
            }

            String eventType = event.get("eventType").asText();
            log.info("Processing event type: {}", eventType);

            switch (eventType) {
                case "ATM_CREATED" -> {
                    String address = event.get("address").asText();
                    String city = event.get("city").asText();

                    Atm atm = new Atm();
                    atm.setAddress(address);
                    atm.setCity(city);

                    Atm saved = atmRepository.save(atm);
                    log.info("*** ATM SAVED with ID: {} ***", saved.getId());
                }

                case "ATM_UPDATED" -> {
                    if (event.has("id")) {
                        Long id = event.get("id").asLong();
                        atmRepository.findById(id).ifPresent(atm -> {
                            if (event.has("address")) atm.setAddress(event.get("address").asText());
                            if (event.has("city")) atm.setCity(event.get("city").asText());
                            atmRepository.save(atm);
                            log.info("ATM updated: {}", id);
                        });
                    }
                }

                case "ATM_DELETED" -> {
                    if (event.has("id")) {
                        Long id = event.get("id").asLong();
                        atmRepository.deleteById(id);
                        log.info("ATM deleted: {}", id);
                    }
                }

                default -> log.warn("Unknown event type: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Failed to process message: {}", message, e);
            e.printStackTrace();
        }
    }
}