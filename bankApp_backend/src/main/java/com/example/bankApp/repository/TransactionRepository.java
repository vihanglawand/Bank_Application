package com.example.bankApp.repository;

import com.example.bankApp.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromAccountOrToAccount(int fromAcc, int toAcc);
}