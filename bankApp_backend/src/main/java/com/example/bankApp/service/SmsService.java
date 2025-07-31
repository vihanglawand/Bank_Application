package com.example.bankApp.service;

public interface SmsService {
    void sendSms(String phoneNumber, String message);
}