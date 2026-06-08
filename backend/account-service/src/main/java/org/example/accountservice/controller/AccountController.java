package org.example.accountservice.controller;

import org.example.accountservice.model.BusinessAccount;
import org.example.accountservice.model.PersonalAccount;
import org.example.accountservice.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // Создание личного счёта
    @PostMapping("/personal")
    public ResponseEntity<PersonalAccount> createPersonalAccount(
            @RequestParam Long ownerId,
            @RequestParam(defaultValue = "RUB") String currency,
            @RequestHeader("X-Manager-Key") String managerKey) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createPersonalAccount(ownerId, currency, managerKey));
    }

    // Создание счёта юрлица
    @PostMapping("/business")
    public ResponseEntity<BusinessAccount> createBusinessAccount(
            @RequestParam Long ownerId,
            @RequestParam(defaultValue = "RUB") String currency,
            @RequestHeader("X-Manager-Key") String managerKey) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createBusinessAccount(ownerId, currency, managerKey));
    }

    // Закрытие личного счёта
    @DeleteMapping("/personal/{id}")
    public ResponseEntity<Map<String, String>> closePersonalAccount(
            @PathVariable Long id,
            @RequestHeader("X-Manager-Key") String managerKey) {
        accountService.closePersonalAccount(id, managerKey);
        return ResponseEntity.ok(Map.of("status", "closed"));
    }

    // Закрытие счёта юрлица
    @DeleteMapping("/business/{id}")
    public ResponseEntity<Map<String, String>> closeBusinessAccount(
            @PathVariable Long id,
            @RequestHeader("X-Manager-Key") String managerKey) {
        accountService.closeBusinessAccount(id, managerKey);
        return ResponseEntity.ok(Map.of("status", "closed"));
    }

    // Транзакция между личными счетами
    @PostMapping("/transfer/personal")
    public ResponseEntity<Map<String, String>> transferPersonal(
            @RequestParam Long from,
            @RequestParam Long to,
            @RequestParam BigDecimal amount,
            @RequestHeader("X-Manager-Key") String managerKey) {
        accountService.transferBetweenPersonalAccounts(from, to, amount, managerKey);
        return ResponseEntity.ok(Map.of("status", "transfer completed"));
    }

    // Транзакция между счетами юрлиц
    @PostMapping("/transfer/business")
    public ResponseEntity<Map<String, String>> transferBusiness(
            @RequestParam Long from,
            @RequestParam Long to,
            @RequestParam BigDecimal amount,
            @RequestHeader("X-Manager-Key") String managerKey) {
        accountService.transferBetweenBusinessAccounts(from, to, amount, managerKey);
        return ResponseEntity.ok(Map.of("status", "transfer completed"));
    }

    // Получение личных счетов владельца
    @GetMapping("/personal/owner/{ownerId}")
    public ResponseEntity<List<PersonalAccount>> getPersonalAccountsByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(accountService.getPersonalAccountsByOwner(ownerId));
    }

    // Получение счетов юрлица
    @GetMapping("/business/owner/{ownerId}")
    public ResponseEntity<List<BusinessAccount>> getBusinessAccountsByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(accountService.getBusinessAccountsByOwner(ownerId));
    }
}