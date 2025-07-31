package com.example.bankApp.controller;

import com.example.bankApp.dto.TransferRequest;
import com.example.bankApp.model.Transaction;
import com.example.bankApp.service.BankService;
import com.example.bankApp.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final BankService bankService;
    private final UserRepository userRepository;

    public AccountController(BankService bankService, UserRepository userRepository) {
        this.bankService = bankService;
        this.userRepository = userRepository;
    }

    // ✅ Check balance
    @GetMapping("/{accNo}/balance")
    public ResponseEntity<Double> checkBalance(@PathVariable int accNo) {
        return ResponseEntity.ok(bankService.checkBalance(accNo)
                .orElseThrow(() -> new RuntimeException("Account not found")));
    }

    // ✅ Deposit
    @PostMapping("/{accNo}/deposit")
    public ResponseEntity<Boolean> deposit(
            @PathVariable int accNo,
            @RequestParam double amount) {
        return ResponseEntity.ok(bankService.deposit(accNo, amount));
    }

    // ✅ Withdraw
    @PostMapping("/{accNo}/withdraw")
    public ResponseEntity<Boolean> withdraw(
            @PathVariable int accNo,
            @RequestParam double amount) {
        return ResponseEntity.ok(bankService.withdraw(accNo, amount));
    }

    // ✅ Transfer
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(
            @Valid @RequestBody TransferRequest request,
            Principal principal) {
        try {
            String fromUsername = principal.getName();
            bankService.transfer(request, fromUsername);
            return ResponseEntity.ok("Transfer successful.");
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Transfer failed: " + ex.getMessage());
        }
    }

    // ✅ Transaction history
    @GetMapping("/{accNo}/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable int accNo) {
        return ResponseEntity.ok(bankService.getTransactionHistory(accNo));
    }
}
