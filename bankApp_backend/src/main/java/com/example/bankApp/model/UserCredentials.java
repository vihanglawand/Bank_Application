package com.example.bankApp.model;

import jakarta.persistence.*;

@Entity
public class UserCredentials {

    @Id
    private String username;

    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @OneToOne
    @JoinColumn(name = "acc_no")
    private AccountDetails account;

    // Getters and Setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public AccountDetails getAccount() {
        return account;
    }

    public void setAccount(AccountDetails account) {
        this.account = account;
    }
}
