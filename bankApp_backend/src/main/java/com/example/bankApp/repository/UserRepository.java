package com.example.bankApp.repository;

import com.example.bankApp.model.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserCredentials, String> {
    Optional<UserCredentials> findByUsername(String username);

    // Add this for forgot password
    Optional<UserCredentials> findByUsernameAndPhoneNumber(String username, String phoneNumber);
}
