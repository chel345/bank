package org.example.branch_service.controller;

import org.example.branch_service.model.Branch;
import org.example.branch_service.repository.BranchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/branches")
public class BranchQueryController {

    private final BranchRepository repository;

    public BranchQueryController(BranchRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<Map<String, Object>> getByCity(
            @PathVariable String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Branch> branchPage = repository.findByCityIgnoreCase(city, PageRequest.of(page, size));

        Map<String, Object> response = new HashMap<>();
        response.put("branches", branchPage.getContent());
        response.put("currentPage", branchPage.getNumber());
        response.put("totalItems", branchPage.getTotalElements());
        response.put("totalPages", branchPage.getTotalPages());
        response.put("city", city);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Branch> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}