package com.example.bankApp.repository;

import com.example.bankApp.model.AccountDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountDetails, Integer> {
}