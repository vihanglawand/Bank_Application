package com.example.bankApp.repository;

import com.example.bankApp.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByUsernameAndToken(String username, String token);
    void deleteByUsername(String username);
}