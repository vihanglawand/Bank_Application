package com.example.bankApp.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int fromAccount;
    private int toAccount;
    private double amount;
    private String type; // "DEPOSIT", "WITHDRAWAL", "TRANSFER"
    private LocalDateTime timestamp = LocalDateTime.now();
}