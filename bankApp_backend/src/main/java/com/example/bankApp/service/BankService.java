package com.example.bankApp.service;
import com.example.bankApp.dto.TransferRequest;

import com.example.bankApp.model.*;
import com.example.bankApp.repository.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BankService {

    private final AccountRepository accountRepo;
    private final UserRepository userRepo;
    private final TransactionRepository transactionRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public BankService(AccountRepository accountRepo,
                       UserRepository userRepo,
                       TransactionRepository transactionRepo,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager) {
        this.accountRepo = accountRepo;
        this.userRepo = userRepo;
        this.transactionRepo = transactionRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public String signup(UserCredentials user) {
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Create and save new account
        AccountDetails account = new AccountDetails();
        account.setName(user.getUsername());
        account.setBalance(0.0);
        AccountDetails savedAccount = accountRepo.save(account);

        // Set password, mobile, account
        user.setAccount(savedAccount);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepo.save(user);

        return "Account created for user: " + user.getUsername() + " with Account No: " + savedAccount.getAccNo();
    }

    public String login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtTokenProvider.generateToken(username);
    }

    public Optional<Double> checkBalance(int accNo) {
        return accountRepo.findById(accNo).map(AccountDetails::getBalance);
    }

    @Transactional
    public boolean deposit(int accNo, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        return accountRepo.findById(accNo).map(account -> {
            account.setBalance(account.getBalance() + amount);
            accountRepo.save(account);

            Transaction transaction = new Transaction();
            transaction.setFromAccount(0); // System account
            transaction.setToAccount(accNo);
            transaction.setAmount(amount);
            transaction.setType("DEPOSIT");
            transactionRepo.save(transaction);

            return true;
        }).orElse(false);
    }

    @Transactional
    public boolean withdraw(int accNo, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        return accountRepo.findById(accNo).map(account -> {
            if (account.getBalance() >= amount) {
                account.setBalance(account.getBalance() - amount);
                accountRepo.save(account);

                Transaction transaction = new Transaction();
                transaction.setFromAccount(accNo);
                transaction.setToAccount(0); // System account
                transaction.setAmount(amount);
                transaction.setType("WITHDRAWAL");
                transactionRepo.save(transaction);

                return true;
            }
            throw new RuntimeException("Insufficient balance");
        }).orElse(false);
    }

    public void transfer(TransferRequest request, String fromUsername) {
        AccountDetails fromAccount = accountRepo.findByName(fromUsername)
                .orElseThrow(() -> new RuntimeException("Sender account not found"));

        AccountDetails toAccount = accountRepo.findById(request.getToAccountId())
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));

        double amount = request.getAmount();

        if (fromAccount.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);

        accountRepo.save(fromAccount);
        accountRepo.save(toAccount);

        Transaction txn = new Transaction();
        txn.setFromAccount(fromAccount.getAccNo());
        txn.setToAccount(toAccount.getAccNo());
        txn.setAmount(amount);
        txn.setType("TRANSFER");
        txn.setTimestamp(java.time.LocalDateTime.now());

        transactionRepo.save(txn);
    }

    public List<Transaction> getTransactionHistory(int accNo) {
        return transactionRepo.findByFromAccountOrToAccount(accNo, accNo);
    }


}
