package org.example.apigateway.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class TransferRequest {

    @NotNull
    private Long fromAccountId;

    @NotNull
    private Long toAccountId;

    @NotNull @Positive
    private BigDecimal amount;

    @NotNull
    private String accountType; // "personal" или "business"

    public Long getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(Long v) { this.fromAccountId = v; }
    public Long getToAccountId() { return toAccountId; }
    public void setToAccountId(Long v) { this.toAccountId = v; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal v) { this.amount = v; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String v) { this.accountType = v; }
}