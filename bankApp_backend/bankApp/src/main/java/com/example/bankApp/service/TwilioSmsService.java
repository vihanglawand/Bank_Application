package com.example.bankApp.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.twilio.Twilio;

@Service
public class TwilioSmsService implements SmsService {
    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromNumber;

    public TwilioSmsService() {
        Twilio.init(accountSid, authToken);
    }

    @Override
    public void sendSms(String phoneNumber, String message) {
        Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(fromNumber),
                message
        ).create();
    }
}