package org.example.branch_service.repository;

import org.example.branch_service.model.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    Page<Branch> findByCityIgnoreCase(String city, Pageable pageable);
}