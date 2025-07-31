package com.example.bankApp.controller;

import com.example.bankApp.model.Transaction;
import com.example.bankApp.service.BankService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final BankService bankService;

    public AccountController(BankService bankService) {
        this.bankService = bankService;
    }

    @GetMapping("/{accNo}/balance")
    public ResponseEntity<Double> checkBalance(@PathVariable int accNo) {
        return ResponseEntity.ok(bankService.checkBalance(accNo).orElseThrow());
    }

    @PostMapping("/{accNo}/deposit")
    public ResponseEntity<Boolean> deposit(
            @PathVariable int accNo,
            @RequestParam double amount) {
        return ResponseEntity.ok(bankService.deposit(accNo, amount));
    }

    @PostMapping("/{accNo}/withdraw")
    public ResponseEntity<Boolean> withdraw(
            @PathVariable int accNo,
            @RequestParam double amount) {
        return ResponseEntity.ok(bankService.withdraw(accNo, amount));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Boolean> transfer(
            @RequestParam int fromAccNo,
            @RequestParam int toAccNo,
            @RequestParam double amount) {
        return ResponseEntity.ok(bankService.transfer(fromAccNo, toAccNo, amount));
    }

    @GetMapping("/{accNo}/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable int accNo) {
        return ResponseEntity.ok(bankService.getTransactionHistory(accNo));
    }
}