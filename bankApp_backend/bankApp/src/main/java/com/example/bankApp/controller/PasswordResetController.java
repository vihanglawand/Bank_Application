package com.example.bankApp.controller;

import com.example.bankApp.service.BankService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password-reset")
public class PasswordResetController {
    private final BankService bankService;

    public PasswordResetController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<String> initiatePasswordReset(@RequestParam String username) {
        return ResponseEntity.ok(bankService.initiatePasswordReset(username));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Boolean> resetPassword(
            @RequestParam String username,
            @RequestParam String otp,
            @RequestParam String newPassword) {
        return ResponseEntity.ok(bankService.resetPassword(username, otp, newPassword));
    }
}