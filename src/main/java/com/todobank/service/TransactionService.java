package com.todobank.service;

import com.todobank.entity.Account;
import com.todobank.entity.Transaction;
import com.todobank.repository.TransactionRepository;
import com.todobank.util.TransactionType;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction transferMoney(Account from, Account to, Double amount, String description) {
        Transaction tx = new Transaction();
        tx.setFromAccount(from);
        tx.setToAccount(to);
        tx.setAmount(amount);
        tx.setDescription(description);
        tx.setTransactionDate(LocalDateTime.now());
        tx.setTransactionType(TransactionType.TRANSFER);
        tx.setBalanceAfter(from.getBalance() - amount);
        tx.setRisky(amount > 1000);

        return transactionRepository.save(tx);
    }

    public List<Transaction> getTransactionsForAccount(Account account) {
        return transactionRepository.findByFromAccountOrderByTransactionDateDesc(account);
    }

    public Transaction saveTransaction(Account account, TransactionType type, Double amount, Double newBalance, String description) {
        String transactionDescription = (description != null && !description.trim().isEmpty())
                ? description
                : generateDefaultDescription(type, amount);

        Transaction transaction = new Transaction();
        transaction.setFromAccount(account);
        transaction.setAmount(amount);
        transaction.setDescription(transactionDescription);
        transaction.setTransactionType(type);
        transaction.setBalanceAfter(newBalance);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setRisky(amount > 1000);

        return transactionRepository.save(transaction);
    }

    private String generateDefaultDescription(TransactionType type, Double amount) {
        switch (type) {
            case DEPOSIT:
                return "Deposit of " + amount + " RON";
            case WITHDRAW:
                return "Withdrawal of " + amount + " RON";
            case TRANSFER:
                return "Transfer of " + amount + " RON";
            default:
                return "Transaction of " + amount + " RON";
        }
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    public List<Transaction> getRiskyTransactions() {
        return transactionRepository.findByRiskyTrue();
    }

    public void save(Transaction outgoing) {
        transactionRepository.save(outgoing);
    }
}