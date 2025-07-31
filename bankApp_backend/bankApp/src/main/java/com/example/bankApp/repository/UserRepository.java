package com.example.bankApp.repository;

import com.example.bankApp.model.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserCredentials, Long> {
    Optional<UserCredentials> findByUsername(String username);
    Optional<UserCredentials> findByUsernameAndPassword(String username, String password);
}