package org.example.clientservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "client_legal_entity", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"client_id", "legal_entity_id"})
})
public class ClientLegalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "legal_entity_id", nullable = false)
    private Long legalEntityId;

    public ClientLegalEntity() {}

    public ClientLegalEntity(Long clientId, Long legalEntityId) {
        this.clientId = clientId;
        this.legalEntityId = legalEntityId;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getLegalEntityId() { return legalEntityId; }
    public void setLegalEntityId(Long legalEntityId) { this.legalEntityId = legalEntityId; }
}