package org.example.apigateway.controller;

import org.example.apigateway.dto.*;
import org.example.apigateway.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<?>> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(new ErrorResponse(401, "Unauthorized", e.getMessage()))
                ));
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<?>> register(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader("X-Manager-Key") String managerKey) {
        return authService.register(request, managerKey)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ErrorResponse(400, "Bad Request", e.getMessage()))
                ));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}