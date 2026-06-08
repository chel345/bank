package org.example.clientservice.controller;

import org.example.clientservice.dto.ClientCreateRequest;
import org.example.clientservice.dto.ClientUpdateRequest;
import org.example.clientservice.model.Client;
import org.example.clientservice.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    // Создание клиента (только менеджер)
    @PostMapping
    public ResponseEntity<Client> createClient(
            @Valid @RequestBody ClientCreateRequest request,
            @RequestHeader("X-Manager-Key") String managerKey) {
        Client client = clientService.createClient(request, managerKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(client);
    }

    // Чтение клиента по ID
    @GetMapping("/{id}")
    public ResponseEntity<Client> getClient(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    // Чтение клиента по телефону
    @GetMapping("/by-phone/{phone}")
    public ResponseEntity<Client> getClientByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(clientService.getClientByPhone(phone));
    }

    // Обновление клиента
    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientUpdateRequest request,
            @RequestHeader(value = "X-Manager-Key", required = false) String managerKey,
            @RequestHeader(value = "X-Client-Id", required = false) Long clientId) {
        Client client = clientService.updateClient(id, request, managerKey, clientId);
        return ResponseEntity.ok(client);
    }

    // Удаление клиента (только менеджер)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(
            @PathVariable Long id,
            @RequestHeader("X-Manager-Key") String managerKey) {
        clientService.deleteClient(id, managerKey);
        return ResponseEntity.noContent().build();
    }
}