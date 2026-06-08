package org.example.apigateway.dto;

public class TokenResponse {

    private String token;
    private String type = "Bearer";
    private Long clientId;
    private String role;

    public TokenResponse(String token, Long clientId, String role) {
        this.token = token;
        this.clientId = clientId;
        this.role = role;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}