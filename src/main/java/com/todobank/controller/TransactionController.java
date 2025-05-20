package com.todobank.controller;

import com.todobank.entity.Account;
import com.todobank.entity.Transaction;
import com.todobank.entity.User;
import com.todobank.service.AccountService;
import com.todobank.service.TransactionService;
import com.todobank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;
    private final AccountService accountService;

    @Autowired
    public TransactionController(TransactionService transactionService,
                                 UserService userService,
                                 AccountService accountService){
        this.transactionService = transactionService;
        this.userService = userService;
        this.accountService = accountService;
    }

    // Transfer money from one account to another
    @PostMapping("/transfer")
    public Transaction transfer(@RequestParam String fromAccountNumber,
                                @RequestParam String toAccountNumber,
                                @RequestParam Double amount,
                                @RequestParam String description,
                                Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);

        Account fromAccount = accountService.getAccountByNumber(fromAccountNumber);
        Account toAccount = accountService.getAccountByNumber(toAccountNumber);

        // Ensure the logged-in user owns the fromAccount
        if (!fromAccount.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized transfer attempt.");
        }

        return transactionService.transferMoney(fromAccount, toAccount, amount, description);
    }

    // Get transactions for a specific account
    @GetMapping("/list")
    public List<Transaction> getTransactions(@RequestParam String accountNumber, Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);

        Account account = accountService.getAccountByNumber(accountNumber);
        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access.");
        }
        return transactionService.getTransactionsForAccount(account);
    }


}