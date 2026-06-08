package org.example.accountservice.service;

import org.example.accountservice.model.BusinessAccount;
import org.example.accountservice.model.PersonalAccount;
import org.example.accountservice.repository.BusinessAccountRepository;
import org.example.accountservice.repository.PersonalAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountService {

    private final PersonalAccountRepository personalAccountRepository;
    private final BusinessAccountRepository businessAccountRepository;

    @Value("${manager.secret.key}")
    private String managerSecretKey;

    public AccountService(PersonalAccountRepository personalAccountRepository,
                          BusinessAccountRepository businessAccountRepository) {
        this.personalAccountRepository = personalAccountRepository;
        this.businessAccountRepository = businessAccountRepository;
    }

    // Создание личного счёта
    public PersonalAccount createPersonalAccount(Long ownerId, String currency, String managerKey) {
        if (!managerSecretKey.equals(managerKey)) {
            throw new SecurityException("Only manager can create accounts");
        }

        PersonalAccount account = new PersonalAccount();
        account.setOwnerId(ownerId);
        account.setCurrency(currency != null ? currency : "RUB");
        account.setActive(true);
        account.setBalance(BigDecimal.ZERO);

        return personalAccountRepository.save(account);
    }

    // Создание счёта юрлица
    public BusinessAccount createBusinessAccount(Long ownerId, String currency, String managerKey) {
        if (!managerSecretKey.equals(managerKey)) {
            throw new SecurityException("Only manager can create accounts");
        }

        BusinessAccount account = new BusinessAccount();
        account.setOwnerId(ownerId);
        account.setCurrency(currency != null ? currency : "RUB");
        account.setActive(true);
        account.setBalance(BigDecimal.ZERO);

        return businessAccountRepository.save(account);
    }

    // Закрытие личного счёта
    @Transactional
    public void closePersonalAccount(Long accountId, String managerKey) {
        if (!managerSecretKey.equals(managerKey)) {
            throw new SecurityException("Only manager can close accounts");
        }

        PersonalAccount account = personalAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setActive(false);
        personalAccountRepository.save(account);
    }

    // Закрытие счёта юрлица
    @Transactional
    public void closeBusinessAccount(Long accountId, String managerKey) {
        if (!managerSecretKey.equals(managerKey)) {
            throw new SecurityException("Only manager can close accounts");
        }

        BusinessAccount account = businessAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setActive(false);
        businessAccountRepository.save(account);
    }

    // Транзакция между личными счетами
    @Transactional
    public void transferBetweenPersonalAccounts(Long fromAccountId, Long toAccountId,
                                                BigDecimal amount, String managerKey) {
        if (!managerSecretKey.equals(managerKey)) {
            throw new SecurityException("Only manager can perform transfers");
        }

        PersonalAccount from = personalAccountRepository.findById(fromAccountId)
                .orElseThrow(() -> new RuntimeException("Source account not found"));
        PersonalAccount to = personalAccountRepository.findById(toAccountId)
                .orElseThrow(() -> new RuntimeException("Target account not found"));

        if (!from.isActive() || !to.isActive()) {
            throw new RuntimeException("One of accounts is closed");
        }

        if (from.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        personalAccountRepository.save(from);
        personalAccountRepository.save(to);
    }

    // Транзакция между счетами юрлиц
    @Transactional
    public void transferBetweenBusinessAccounts(Long fromAccountId, Long toAccountId,
                                                BigDecimal amount, String managerKey) {
        if (!managerSecretKey.equals(managerKey)) {
            throw new SecurityException("Only manager can perform transfers");
        }

        BusinessAccount from = businessAccountRepository.findById(fromAccountId)
                .orElseThrow(() -> new RuntimeException("Source account not found"));
        BusinessAccount to = businessAccountRepository.findById(toAccountId)
                .orElseThrow(() -> new RuntimeException("Target account not found"));

        if (!from.isActive() || !to.isActive()) {
            throw new RuntimeException("One of accounts is closed");
        }

        if (from.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        businessAccountRepository.save(from);
        businessAccountRepository.save(to);
    }

    // Зачисление на личный счёт
    @Transactional
    public void depositToPersonalAccount(Long accountId, BigDecimal amount, String managerKey) {
        if (!managerSecretKey.equals(managerKey)) {
            throw new SecurityException("Only manager can perform transfers");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        PersonalAccount account = personalAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Personal account not found: " + accountId));

        if (!account.isActive()) {
            throw new RuntimeException("Account is closed");
        }

        account.setBalance(account.getBalance().add(amount));
        personalAccountRepository.save(account);
    }

    // Получение счетов клиента
    public java.util.List<PersonalAccount> getPersonalAccountsByOwner(Long ownerId) {
        return personalAccountRepository.findByOwnerIdAndIsActiveTrue(ownerId);
    }

    public java.util.List<BusinessAccount> getBusinessAccountsByOwner(Long ownerId) {
        return businessAccountRepository.findByOwnerIdAndIsActiveTrue(ownerId);
    }
}