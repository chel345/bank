package org.example.apigateway.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter implements Filter {

    private final JwtUtil jwtUtil;
    private final List<String> excludedUrls = List.of(
            "/api/auth/health", "/api/auth/login", "/api/auth/register"
    );

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        if (excludedUrls.stream().anyMatch(path::equals)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String authHeader = httpRequest.getHeader("Authorization");
            String token = jwtUtil.extractToken(authHeader);
            Claims claims = jwtUtil.validateToken(token);

            request.setAttribute("clientId", Long.parseLong(claims.getSubject()));
            request.setAttribute("role", claims.get("role", String.class));

            chain.doFilter(request, response);
        } catch (Exception e) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                    "{\"error\":\"Unauthorized\",\"message\":\"" + e.getMessage() + "\"}"
            );
        }
    }
}