package org.example.clientservice.service;

import org.example.clientservice.dto.ClientCreateRequest;
import org.example.clientservice.dto.ClientUpdateRequest;
import org.example.clientservice.model.Client;
import org.example.clientservice.model.ClientLegalEntity;
import org.example.clientservice.repository.ClientLegalEntityRepository;
import org.example.clientservice.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientLegalEntityRepository clientLegalEntityRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${manager.secret.key}")
    private String managerSecretKey;

    public ClientService(ClientRepository clientRepository,
                         ClientLegalEntityRepository clientLegalEntityRepository,
                         PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.clientLegalEntityRepository = clientLegalEntityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Создание клиента (только менеджер)
    public Client createClient(ClientCreateRequest request, String managerKey) {
        if (!managerSecretKey.equals(managerKey)) {
            throw new SecurityException("Only manager can create clients");
        }

        Client client = new Client();
        client.setEmail(request.getEmail());
        client.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        client.setPhone(request.getPhone());
        client.setPassportData(request.getPassportData());

        return clientRepository.save(client);
    }

    // Чтение клиента по ID
    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found: " + id));
    }

    // Чтение клиента по телефону
    public Client getClientByPhone(String phone) {
        return clientRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Client not found by phone: " + phone));
    }

    // Обновление клиента
    @Transactional
    public Client updateClient(Long id, ClientUpdateRequest request, String managerKey, Long authenticatedClientId) {
        Client client = getClientById(id);
        boolean isManager = managerSecretKey.equals(managerKey);
        boolean isOwner = authenticatedClientId != null && authenticatedClientId.equals(id);

        if (!isManager && !isOwner) {
            throw new SecurityException("Access denied");
        }

        // Пользователь может менять только телефон, почту и пароль
        if (isOwner && !isManager) {
            if (request.getEmail() != null) client.setEmail(request.getEmail());
            if (request.getPhone() != null) client.setPhone(request.getPhone());
            if (request.getPassword() != null)
                client.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        // Менеджер может менять всё, включая паспортные данные
        if (isManager) {
            if (request.getEmail() != null) client.setEmail(request.getEmail());
            if (request.getPhone() != null) client.setPhone(request.getPhone());
            if (request.getPassword() != null)
                client.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            if (request.getPassportData() != null) client.setPassportData(request.getPassportData());
        }

        return clientRepository.save(client);
    }

    // Удаление клиента (только менеджер)
    @Transactional
    public void deleteClient(Long id, String managerKey) {
        if (!managerSecretKey.equals(managerKey)) {
            throw new SecurityException("Only manager can delete clients");
        }

        // Удаляем все связи
        List<ClientLegalEntity> links = clientLegalEntityRepository.findByClientId(id);
        clientLegalEntityRepository.deleteAll(links);

        clientRepository.deleteById(id);
    }

    // Получить юрлица, связанные с клиентом
    public List<Long> getClientLegalEntityIds(Long clientId) {
        return clientLegalEntityRepository.findByClientId(clientId)
                .stream()
                .map(ClientLegalEntity::getLegalEntityId)
                .toList();
    }

    // Проверка связи клиента с юрлицом
    public boolean isClientLinkedToLegalEntity(Long clientId, Long legalEntityId) {
        return clientLegalEntityRepository.existsByClientIdAndLegalEntityId(clientId, legalEntityId);
    }
}