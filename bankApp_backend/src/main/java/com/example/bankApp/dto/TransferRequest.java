package com.example.bankApp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class TransferRequest {
    @NotNull(message = "Target account is required")
    private Integer toAccountId;

    @Min(value = 1, message = "Amount must be at least 1")
    private double amount;

    public Integer getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Integer toAccountId) {
        this.toAccountId = toAccountId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
