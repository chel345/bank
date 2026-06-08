package org.example.cardservice.kafka;

import org.example.cardservice.model.Card;
import org.example.cardservice.repository.CardRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CardCommandConsumer {

    private static final Logger log = LoggerFactory.getLogger(CardCommandConsumer.class);
    private final CardRepository repository;
    private final ObjectMapper objectMapper;

    @Value("${manager.secret.key}")
    private String managerSecretKey;

    public CardCommandConsumer(CardRepository repository) {
        this.repository = repository;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "card-commands", groupId = "card-service")
    @Transactional
    public void handleCommand(String message) {
        log.info("Received card command: {}", message);

        try {
            JsonNode cmd = objectMapper.readTree(message);
            String type = cmd.get("type").asText();
            String key = cmd.get("managerKey").asText();

            if (!managerSecretKey.equals(key)) {
                log.error("Invalid manager key for command: {}", type);
                return;
            }

            switch (type) {
                case "CREATE_CARD" -> {
                    Card card = new Card();
                    card.setAccountId(cmd.get("accountId").asLong());
                    card.setCardNumber(cmd.get("cardNumber").asText());

                    Card saved = repository.save(card);
                    log.info("Card created with ID: {}", saved.getId());
                }

                case "UPDATE_CARD" -> {
                    Long id = cmd.get("id").asLong();
                    repository.findById(id).ifPresent(card -> {
                        if (cmd.has("accountId")) card.setAccountId(cmd.get("accountId").asLong());
                        if (cmd.has("cardNumber")) card.setCardNumber(cmd.get("cardNumber").asText());
                        repository.save(card);
                        log.info("Card updated: {}", id);
                    });
                }

                case "DELETE_CARD" -> {
                    Long id = cmd.get("id").asLong();
                    repository.deleteById(id);
                    log.info("Card deleted: {}", id);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process card command", e);
        }
    }
}