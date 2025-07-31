package com.example.bankApp.repository;

import com.example.bankApp.model.AccountDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountDetails, Integer> {
    Optional<AccountDetails> findByName(String name);

}