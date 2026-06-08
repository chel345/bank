package com.example.atm.controller;

import com.example.atm.model.Atm;
import com.example.atm.repository.AtmRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/atms")
public class AtmQueryController {

    private final AtmRepository atmRepository;

    public AtmQueryController(AtmRepository atmRepository) {
        this.atmRepository = atmRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Atm>> getAllAtms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(atmRepository.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Atm> getAtmById(@PathVariable Long id) {
        return atmRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<Map<String, Object>> getAtmsByCity(
            @PathVariable String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Atm> atmPage = atmRepository.findByCityIgnoreCase(city, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("atms", atmPage.getContent());
        response.put("currentPage", atmPage.getNumber());
        response.put("totalItems", atmPage.getTotalElements());
        response.put("totalPages", atmPage.getTotalPages());
        response.put("city", city);

        return ResponseEntity.ok(response);
    }
}