package com.example.bankApp.controller;

import com.example.bankApp.model.LoginRequest;
import com.example.bankApp.model.AccountDetails;
import com.example.bankApp.model.UserCredentials;
import com.example.bankApp.repository.AccountRepository;
import com.example.bankApp.repository.UserRepository;
import com.example.bankApp.service.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AccountRepository accountRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
    }

    @PostMapping("/signup")
    @Transactional
    public ResponseEntity<Map<String, Object>> signup(@RequestBody LoginRequest signUpRequest) {
        try {
            if (userRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Username is already taken!");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Create and save account
            AccountDetails newAccount = new AccountDetails();
            newAccount.setName(signUpRequest.getUsername());
            newAccount.setBalance(0.0);
            accountRepository.save(newAccount);

            // Create user
            UserCredentials newUser = new UserCredentials();
            newUser.setUsername(signUpRequest.getUsername());
            newUser.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            newUser.setPhoneNumber(signUpRequest.getPhoneNumber());
            newUser.setAccount(newAccount);
            userRepository.save(newUser);

            // Prepare response (using HashMap to avoid NullPointerException)
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("username", newUser.getUsername());
            response.put("phone_number", newUser.getPhoneNumber());
            response.put("accountNumber", newAccount.getAccNo());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Signup failed: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            String token = jwtTokenProvider.generateToken(loginRequest.getUsername());

            UserCredentials user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", user.getUsername());
            response.put("phone_number", user.getPhoneNumber());
            response.put("account_number", user.getAccount().getAccNo());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid username or password");
            return ResponseEntity.status(HttpServletResponse.SC_FORBIDDEN).body(errorResponse);
        }
    }

    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String phoneNumber = payload.get("phonenumber"); // should match frontend field
        String otp = payload.get("otp");
        String newPassword = payload.get("newPassword");

        if (!"123456".equals(otp)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid OTP"));
        }

        return userRepository.findByUsernameAndPhoneNumber(username, phoneNumber)
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
                })
                .orElse(ResponseEntity.badRequest()
                        .body(Map.of("error", "User not found with given username and mobile number")));
    }
}
