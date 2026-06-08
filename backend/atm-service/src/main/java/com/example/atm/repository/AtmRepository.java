package com.example.atm.repository;

import com.example.atm.model.Atm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtmRepository extends JpaRepository<Atm, Long> {
    Page<Atm> findByCityIgnoreCase(String city, Pageable pageable);
    boolean existsByAddressAndCity(String address, String city);
}