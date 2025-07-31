package com.example.bankApp.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_details")
@Data
public class AccountDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_seq")
    @GenericGenerator(
            name = "account_seq",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "account_sequence"),
                    @Parameter(name = "initial_value", value = "1000100"),
                    @Parameter(name = "increment_size", value = "1")
            }
    )
    @Column(name = "acc_no", unique = true, nullable = false)
    private int accNo;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "balance", nullable = false)
    private double balance = 0.0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "account_type")
    private String accountType = "SAVINGS"; // Can be SAVINGS, CURRENT, etc.

    @Column(name = "status")
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, DORMANT

    // Constructors
    public AccountDetails() {
    }

    public AccountDetails(String name) {
        this.name = name;
    }

    // Custom business methods
    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }

    public void activateAccount() {
        this.status = "ACTIVE";
    }

    public void deactivateAccount() {
        this.status = "INACTIVE";
    }
}