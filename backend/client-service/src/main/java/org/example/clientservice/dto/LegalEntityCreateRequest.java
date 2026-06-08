package org.example.clientservice.dto;

import jakarta.validation.constraints.NotBlank;

public class LegalEntityCreateRequest {

    @NotBlank
    private String organizationName;

    @NotBlank
    private String address;

    @NotBlank
    private String phone;

    private String inn;
    private Long clientId; // для автоматической связи

    // Getters and setters
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getInn() { return inn; }
    public void setInn(String inn) { this.inn = inn; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
}