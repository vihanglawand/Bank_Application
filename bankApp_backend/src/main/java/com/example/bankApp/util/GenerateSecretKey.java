package com.example.bankApp.util;

import java.security.SecureRandom;
import java.util.Base64;

public class GenerateSecretKey {
    public static void main(String[] args) {
        byte[] key = new byte[64]; // 512-bit key for HS512
        new SecureRandom().nextBytes(key);
        String base64Key = Base64.getEncoder().encodeToString(key);
        System.out.println("Base64 Secret Key: " + base64Key);
    }
}
