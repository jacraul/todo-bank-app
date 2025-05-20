package com.todobank.entity;

import com.todobank.util.TransactionType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;

    @ManyToOne
    @JoinColumn(name = "to_account_id")
    private Account toAccount;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(nullable = false)
    private boolean risky;

    @Column(name = "balance_after", nullable = false)
    private Double balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    // Default constructor
    public Transaction() {
        this.transactionDate = LocalDateTime.now();
        this.risky = false;
    }

    // Constructor with essential fields
    public Transaction(Account fromAccount, Double amount, String description,
                       TransactionType transactionType, Double balanceAfter) {
        this();
        this.fromAccount = fromAccount;
        this.amount = amount;
        this.description = description;
        this.transactionType = transactionType;
        this.balanceAfter = balanceAfter;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(Account fromAccount) {
        this.fromAccount = fromAccount;
    }

    public Account getToAccount() {
        return toAccount;
    }

    public void setToAccount(Account toAccount) {
        this.toAccount = toAccount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public boolean isRisky() {
        return risky;
    }

    public void setRisky(boolean risky) {
        this.risky = risky;
    }

    public Double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(Double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", fromAccount=" + fromAccount +
                ", toAccount=" + toAccount +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", transactionDate=" + transactionDate +
                ", risky=" + risky +
                ", balanceAfter=" + balanceAfter +
                ", transactionType=" + transactionType +
                '}';
    }

    public void setSourceAccount(Account sourceAccount) {
        this.fromAccount = sourceAccount;
    }

    public void setTargetAccount(Account targetAccount) {
        this.toAccount = targetAccount;
    }

    public void setType(String transferOut) {
        if (transferOut.equals("Transfer Out")) {
            this.transactionType = TransactionType.TRANSFER_OUT;
        } else if (transferOut.equals("Transfer In")) {
            this.transactionType = TransactionType.TRANSFER_IN;
        } else {
            throw new IllegalArgumentException("Invalid transaction type: " + transferOut);
        }
    }

    public void setTimestamp(LocalDateTime now) {
        this.transactionDate = now;
    }
}