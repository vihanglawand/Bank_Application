package com.example.bankApp.service;

import com.example.bankApp.model.*;
import com.example.bankApp.repository.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BankService {

    private final AccountRepository accountRepo;
    private final UserRepository userRepo;
    private final TransactionRepository transactionRepo;
    private final PasswordResetTokenRepository passwordResetTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final OtpService otpService;
    private final SmsService smsService;
    private final AuthenticationManager authenticationManager;

    public BankService(AccountRepository accountRepo,
                       UserRepository userRepo,
                       TransactionRepository transactionRepo,
                       PasswordResetTokenRepository passwordResetTokenRepo,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       OtpService otpService,
                       SmsService smsService,
                       AuthenticationManager authenticationManager) {
        this.accountRepo = accountRepo;
        this.userRepo = userRepo;
        this.transactionRepo = transactionRepo;
        this.passwordResetTokenRepo = passwordResetTokenRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.otpService = otpService;
        this.smsService = smsService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public String signup(UserCredentials user) {
        // Check if username already exists
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Create new account
        AccountDetails account = new AccountDetails();
        account.setName(user.getUsername());
        account.setBalance(0.0);
        AccountDetails savedAccount = accountRepo.save(account);

        // Save user with encoded password
        user.setAccNo(savedAccount.getAccNo());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);

        return "Account created successfully. Account No: " + savedAccount.getAccNo();
    }

    public String login(String username, String password) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
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

        Optional<AccountDetails> accountOpt = accountRepo.findById(accNo);
        if (accountOpt.isPresent()) {
            AccountDetails account = accountOpt.get();
            account.setBalance(account.getBalance() + amount);
            accountRepo.save(account);

            // Record transaction
            Transaction transaction = new Transaction();
            transaction.setFromAccount(0); // System account
            transaction.setToAccount(accNo);
            transaction.setAmount(amount);
            transaction.setType("DEPOSIT");
            transactionRepo.save(transaction);

            return true;
        }
        return false;
    }

    @Transactional
    public boolean withdraw(int accNo, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Optional<AccountDetails> accountOpt = accountRepo.findById(accNo);
        if (accountOpt.isPresent()) {
            AccountDetails account = accountOpt.get();
            if (account.getBalance() >= amount) {
                account.setBalance(account.getBalance() - amount);
                accountRepo.save(account);

                // Record transaction
                Transaction transaction = new Transaction();
                transaction.setFromAccount(accNo);
                transaction.setToAccount(0); // System account
                transaction.setAmount(amount);
                transaction.setType("WITHDRAWAL");
                transactionRepo.save(transaction);

                return true;
            }
            throw new RuntimeException("Insufficient balance");
        }
        return false;
    }

    @Transactional
    public boolean transfer(int fromAccNo, int toAccNo, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (fromAccNo == toAccNo) {
            throw new IllegalArgumentException("Cannot transfer to same account");
        }

        Optional<AccountDetails> fromAccountOpt = accountRepo.findById(fromAccNo);
        Optional<AccountDetails> toAccountOpt = accountRepo.findById(toAccNo);

        if (fromAccountOpt.isPresent() && toAccountOpt.isPresent()) {
            AccountDetails fromAccount = fromAccountOpt.get();
            AccountDetails toAccount = toAccountOpt.get();

            if (fromAccount.getBalance() >= amount) {
                // Deduct from sender
                fromAccount.setBalance(fromAccount.getBalance() - amount);
                accountRepo.save(fromAccount);

                // Add to receiver
                toAccount.setBalance(toAccount.getBalance() + amount);
                accountRepo.save(toAccount);

                // Record transaction
                Transaction transaction = new Transaction();
                transaction.setFromAccount(fromAccNo);
                transaction.setToAccount(toAccNo);
                transaction.setAmount(amount);
                transaction.setType("TRANSFER");
                transactionRepo.save(transaction);

                return true;
            }
            throw new RuntimeException("Insufficient balance");
        }
        throw new RuntimeException("One or both accounts not found");
    }

    public List<Transaction> getTransactionHistory(int accNo) {
        return transactionRepo.findByFromAccountOrToAccountOrderByTimestampDesc(accNo, accNo);
    }

    public Optional<AccountDetails> getAccountDetails(int accNo) {
        return accountRepo.findById(accNo);
    }

    @Transactional
    public String initiatePasswordReset(String username) {
        Optional<UserCredentials> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        // Generate OTP
        String otp = otpService.generateOtp();

        // Invalidate any existing tokens
        passwordResetTokenRepo.deleteByUsername(username);

        // Save new token with 15-minute expiry
        PasswordResetToken token = new PasswordResetToken();
        token.setUsername(username);
        token.setToken(otp);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        passwordResetTokenRepo.save(token);

        // Send OTP via SMS
        UserCredentials user = userOpt.get();
        smsService.sendSms(user.getPhoneNumber(),
                "Your OTP for password reset is: " + otp + ". Valid for 15 minutes.");

        return "OTP sent to registered mobile number";
    }

    @Transactional
    public boolean resetPassword(String username, String otp, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepo
                .findByUsernameAndToken(username, otp);

        if (tokenOpt.isEmpty() || tokenOpt.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        Optional<UserCredentials> userOpt = userRepo.findByUsername(username);
        if (userOpt.isPresent()) {
            UserCredentials user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepo.save(user);

            // Invalidate the used token
            passwordResetTokenRepo.delete(tokenOpt.get());
            return true;
        }
        return false;
    }
}