package org.example.clientservice.service;

import org.example.clientservice.dto.LegalEntityCreateRequest;
import org.example.clientservice.dto.LegalEntityUpdateRequest;
import org.example.clientservice.model.ClientLegalEntity;
import org.example.clientservice.model.LegalEntity;
import org.example.clientservice.repository.ClientLegalEntityRepository;
import org.example.clientservice.repository.LegalEntityRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LegalEntityService {

    private final LegalEntityRepository legalEntityRepository;
    private final ClientLegalEntityRepository clientLegalEntityRepository;
    private final ClientService clientService;

    @Value("${manager.secret.key}")
    private String managerSecretKey;

    public LegalEntityService(LegalEntityRepository legalEntityRepository,
                              ClientLegalEntityRepository clientLegalEntityRepository,
                              ClientService clientService) {
        this.legalEntityRepository = legalEntityRepository;
        this.clientLegalEntityRepository = clientLegalEntityRepository;
        this.clientService = clientService;
    }

    // Создание юрлица (только менеджер)
    @Transactional
    public LegalEntity createLegalEntity(LegalEntityCreateRequest request, String managerKey) {
        if (!managerSecretKey.equals(managerKey)) {
            throw new SecurityException("Only manager can create legal entities");
        }

        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setOrganizationName(request.getOrganizationName());
        legalEntity.setAddress(request.getAddress());
        legalEntity.setPhone(request.getPhone());
        legalEntity.setInn(request.getInn());

        LegalEntity saved = legalEntityRepository.save(legalEntity);

        // Автоматически связываем с клиентом, если указан clientId
        if (request.getClientId() != null) {
            ClientLegalEntity link = new ClientLegalEntity(request.getClientId(), saved.getId());
            clientLegalEntityRepository.save(link);
        }

        return saved;
    }

    // Чтение юрлиц клиента с пагинацией
    public Page<LegalEntity> getLegalEntitiesByClientId(Long clientId, Pageable pageable) {
        return legalEntityRepository.findByClientId(clientId, pageable);
    }

    // Чтение юрлица по ID
    public LegalEntity getLegalEntityById(Long id) {
        return legalEntityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Legal entity not found: " + id));
    }

    // Обновление юрлица
    @Transactional
    public LegalEntity updateLegalEntity(Long id, LegalEntityUpdateRequest request,
                                         String managerKey, Long authenticatedClientId) {
        LegalEntity legalEntity = getLegalEntityById(id);
        boolean isManager = managerSecretKey.equals(managerKey);

        // Проверка: пользователь может редактировать юрлицо, только если связан с ним
        boolean isOwner = authenticatedClientId != null &&
                clientService.isClientLinkedToLegalEntity(authenticatedClientId, id);

        if (!isManager && !isOwner) {
            throw new SecurityException("Access denied");
        }

        // Пользователь может менять всё, кроме ИНН
        if (isOwner && !isManager) {
            if (request.getOrganizationName() != null)
                legalEntity.setOrganizationName(request.getOrganizationName());
            if (request.getAddress() != null) legalEntity.setAddress(request.getAddress());
            if (request.getPhone() != null) legalEntity.setPhone(request.getPhone());
            // ИНН менять нельзя!
        }

        // Менеджер может менять всё
        if (isManager) {
            if (request.getOrganizationName() != null)
                legalEntity.setOrganizationName(request.getOrganizationName());
            if (request.getAddress() != null) legalEntity.setAddress(request.getAddress());
            if (request.getPhone() != null) legalEntity.setPhone(request.getPhone());
            if (request.getInn() != null) legalEntity.setInn(request.getInn());
        }

        return legalEntityRepository.save(legalEntity);
    }

    // Удаление юрлица (только менеджер)
    @Transactional
    public void deleteLegalEntity(Long id, String managerKey) {
        if (!managerSecretKey.equals(managerKey)) {
            throw new SecurityException("Only manager can delete legal entities");
        }

        // Удаляем все связи
        var links = clientLegalEntityRepository.findByLegalEntityId(id);
        clientLegalEntityRepository.deleteAll(links);

        legalEntityRepository.deleteById(id);
    }

    // Связывание клиента с юрлицом (только менеджер)
    @Transactional
    public void linkClientToLegalEntity(Long clientId, Long legalEntityId, String managerKey) {
        if (!managerSecretKey.equals(managerKey)) {
            throw new SecurityException("Only manager can create links");
        }

        if (!clientLegalEntityRepository.existsByClientIdAndLegalEntityId(clientId, legalEntityId)) {
            ClientLegalEntity link = new ClientLegalEntity(clientId, legalEntityId);
            clientLegalEntityRepository.save(link);
        }
    }

    // Разрыв связи (только менеджер)
    @Transactional
    public void unlinkClientFromLegalEntity(Long clientId, Long legalEntityId, String managerKey) {
        if (!managerSecretKey.equals(managerKey)) {
            throw new SecurityException("Only manager can delete links");
        }

        clientLegalEntityRepository.findByClientIdAndLegalEntityId(clientId, legalEntityId)
                .ifPresent(clientLegalEntityRepository::delete);
    }
}