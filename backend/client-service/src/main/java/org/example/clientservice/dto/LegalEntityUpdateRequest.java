package org.example.clientservice.dto;

import jakarta.validation.constraints.NotBlank;

public class LegalEntityUpdateRequest {

    private String organizationName;
    private String address;
    private String phone;
    private String inn;

    // Getters and setters
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getInn() { return inn; }
    public void setInn(String inn) { this.inn = inn; }
}