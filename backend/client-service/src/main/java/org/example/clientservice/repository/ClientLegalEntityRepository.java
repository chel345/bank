package org.example.clientservice.repository;

import org.example.clientservice.model.ClientLegalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientLegalEntityRepository extends JpaRepository<ClientLegalEntity, Long> {
    List<ClientLegalEntity> findByClientId(Long clientId);
    List<ClientLegalEntity> findByLegalEntityId(Long legalEntityId);
    Optional<ClientLegalEntity> findByClientIdAndLegalEntityId(Long clientId, Long legalEntityId);
    boolean existsByClientIdAndLegalEntityId(Long clientId, Long legalEntityId);
}