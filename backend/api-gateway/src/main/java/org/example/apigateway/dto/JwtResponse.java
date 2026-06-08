package org.example.apigateway.dto;

public class JwtResponse {
    private String token;
    private Long userId;

    public JwtResponse(String token, Long userId) {
        this.token = token;
        this.userId = userId;
    }

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
}