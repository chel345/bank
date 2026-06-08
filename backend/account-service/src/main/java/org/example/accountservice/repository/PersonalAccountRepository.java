package org.example.accountservice.repository;

import org.example.accountservice.model.PersonalAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonalAccountRepository extends JpaRepository<PersonalAccount, Long> {
    List<PersonalAccount> findByOwnerId(Long ownerId);
    List<PersonalAccount> findByOwnerIdAndIsActiveTrue(Long ownerId);
}