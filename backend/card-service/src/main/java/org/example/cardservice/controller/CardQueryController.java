package org.example.cardservice.controller;

import org.example.cardservice.model.Card;
import org.example.cardservice.repository.CardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
public class CardQueryController {

    private final CardRepository repository;

    public CardQueryController(CardRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<Map<String, Object>> getByAccount(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Card> cardPage = repository.findByAccountId(accountId, PageRequest.of(page, size));

        Map<String, Object> response = new HashMap<>();
        response.put("cards", cardPage.getContent());
        response.put("currentPage", cardPage.getNumber());
        response.put("totalItems", cardPage.getTotalElements());
        response.put("totalPages", cardPage.getTotalPages());
        response.put("accountId", accountId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Card> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}