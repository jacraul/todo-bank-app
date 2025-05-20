package com.todobank.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber; // e.g., IBAN generated (ROXXTODOxxxxxxxx...)
    private String accountType; // "Personal" or "Business"
    private Double balance;
    private String status;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Account() {}

    public Account(Long id, String accountNumber, String accountType, Double balance, User user) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    private Boolean active;

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

}