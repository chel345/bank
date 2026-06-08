package org.example.clientservice.controller;

import org.example.clientservice.dto.LegalEntityCreateRequest;
import org.example.clientservice.dto.LegalEntityUpdateRequest;
import org.example.clientservice.model.LegalEntity;
import org.example.clientservice.service.LegalEntityService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/legal-entities")
public class LegalEntityController {

    private final LegalEntityService legalEntityService;

    public LegalEntityController(LegalEntityService legalEntityService) {
        this.legalEntityService = legalEntityService;
    }

    // Создание юрлица (только менеджер)
    @PostMapping
    public ResponseEntity<LegalEntity> createLegalEntity(
            @Valid @RequestBody LegalEntityCreateRequest request,
            @RequestHeader("X-Manager-Key") String managerKey) {
        LegalEntity legalEntity = legalEntityService.createLegalEntity(request, managerKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(legalEntity);
    }

    // Чтение юрлиц клиента с пагинацией
    @GetMapping("/by-client/{clientId}")
    public ResponseEntity<Page<LegalEntity>> getLegalEntitiesByClient(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                legalEntityService.getLegalEntitiesByClientId(clientId, PageRequest.of(page, size))
        );
    }

    // Чтение юрлица по ID
    @GetMapping("/{id}")
    public ResponseEntity<LegalEntity> getLegalEntity(@PathVariable Long id) {
        return ResponseEntity.ok(legalEntityService.getLegalEntityById(id));
    }

    // Обновление юрлица
    @PutMapping("/{id}")
    public ResponseEntity<LegalEntity> updateLegalEntity(
            @PathVariable Long id,
            @Valid @RequestBody LegalEntityUpdateRequest request,
            @RequestHeader(value = "X-Manager-Key", required = false) String managerKey,
            @RequestHeader(value = "X-Client-Id", required = false) Long clientId) {
        LegalEntity legalEntity = legalEntityService.updateLegalEntity(id, request, managerKey, clientId);
        return ResponseEntity.ok(legalEntity);
    }

    // Удаление юрлица (только менеджер)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLegalEntity(
            @PathVariable Long id,
            @RequestHeader("X-Manager-Key") String managerKey) {
        legalEntityService.deleteLegalEntity(id, managerKey);
        return ResponseEntity.noContent().build();
    }

    // Связывание клиента с юрлицом (только менеджер)
    @PostMapping("/link")
    public ResponseEntity<Map<String, String>> linkClientToLegalEntity(
            @RequestParam Long clientId,
            @RequestParam Long legalEntityId,
            @RequestHeader("X-Manager-Key") String managerKey) {
        legalEntityService.linkClientToLegalEntity(clientId, legalEntityId, managerKey);
        return ResponseEntity.ok(Map.of("status", "linked"));
    }

    // Разрыв связи (только менеджер)
    @DeleteMapping("/link")
    public ResponseEntity<Map<String, String>> unlinkClientFromLegalEntity(
            @RequestParam Long clientId,
            @RequestParam Long legalEntityId,
            @RequestHeader("X-Manager-Key") String managerKey) {
        legalEntityService.unlinkClientFromLegalEntity(clientId, legalEntityId, managerKey);
        return ResponseEntity.ok(Map.of("status", "unlinked"));
    }
}