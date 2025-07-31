package com.example.bankApp.controller;

import com.example.bankApp.model.UserCredentials;
import com.example.bankApp.service.BankService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final BankService bankService;

    public AuthController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserCredentials user) {
        return ResponseEntity.ok(bankService.signup(user));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestParam String username,
            @RequestParam String password) {
        return ResponseEntity.ok(bankService.login(username, password));
    }
}