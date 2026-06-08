package org.example.clientservice.repository;

import org.example.clientservice.model.LegalEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LegalEntityRepository extends JpaRepository<LegalEntity, Long> {

    @Query("SELECT le FROM LegalEntity le JOIN ClientLegalEntity cle ON le.id = cle.legalEntityId WHERE cle.clientId = :clientId")
    Page<LegalEntity> findByClientId(Long clientId, Pageable pageable);
}