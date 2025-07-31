package com.example.bankApp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_details")
public class AccountDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "acc_no")
    private Integer accNo;

    private String name;

    private Double balance;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    // Getters and Setters

    public Integer getAccNo() {
        return accNo;
    }

    public void setAccNo(Integer accNo) {
        this.accNo = accNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
