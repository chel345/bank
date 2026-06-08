package org.example.branch_service.kafka;

import org.example.branch_service.model.Branch;
import org.example.branch_service.repository.BranchRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BranchCommandConsumer {

    private static final Logger log = LoggerFactory.getLogger(BranchCommandConsumer.class);
    private final BranchRepository repository;
    private final ObjectMapper objectMapper;

    @Value("${manager.secret.key}")
    private String managerSecretKey;

    public BranchCommandConsumer(BranchRepository repository) {
        this.repository = repository;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "branch-commands", groupId = "branch-service")
    @Transactional
    public void handleCommand(String message) {
        log.info("Received branch command: {}", message);

        try {
            JsonNode cmd = objectMapper.readTree(message);
            String type = cmd.get("type").asText();
            String key = cmd.get("managerKey").asText();

            if (!managerSecretKey.equals(key)) {
                log.error("Invalid manager key for command: {}", type);
                return;
            }

            switch (type) {
                case "CREATE_BRANCH" -> {
                    Branch branch = new Branch();
                    branch.setAddress(cmd.get("address").asText());
                    branch.setCity(cmd.get("city").asText());
                    branch.setPhone(cmd.get("phone").asText());
                    branch.setEmail(cmd.has("email") ? cmd.get("email").asText() : null);
                    branch.setWorkingHours(cmd.has("workingHours") ? cmd.get("workingHours").asText() : null);

                    Branch saved = repository.save(branch);
                    log.info("Branch created with ID: {}", saved.getId());
                }

                case "UPDATE_BRANCH" -> {
                    Long id = cmd.get("id").asLong();
                    repository.findById(id).ifPresent(branch -> {
                        if (cmd.has("address")) branch.setAddress(cmd.get("address").asText());
                        if (cmd.has("city")) branch.setCity(cmd.get("city").asText());
                        if (cmd.has("phone")) branch.setPhone(cmd.get("phone").asText());
                        if (cmd.has("email")) branch.setEmail(cmd.get("email").asText());
                        if (cmd.has("workingHours")) branch.setWorkingHours(cmd.get("workingHours").asText());
                        repository.save(branch);
                        log.info("Branch updated: {}", id);
                    });
                }

                case "DELETE_BRANCH" -> {
                    Long id = cmd.get("id").asLong();
                    repository.deleteById(id);
                    log.info("Branch deleted: {}", id);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process branch command", e);
        }
    }
}