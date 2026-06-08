package org.example.accountservice.repository;

import org.example.accountservice.model.BusinessAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessAccountRepository extends JpaRepository<BusinessAccount, Long> {
    List<BusinessAccount> findByOwnerId(Long ownerId);
    List<BusinessAccount> findByOwnerIdAndIsActiveTrue(Long ownerId);
}