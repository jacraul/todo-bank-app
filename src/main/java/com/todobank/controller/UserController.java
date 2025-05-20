package com.todobank.controller;

import com.todobank.entity.Account;
import com.todobank.entity.User;
import com.todobank.service.AccountService;
import org.springframework.ui.Model;
import com.todobank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.todobank.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final AccountService accountService;

    @Autowired
    public UserController(UserService userService, AccountService accountService){
        this.userService = userService;
        this.accountService = accountService;
    }

    // Retrieve the logged-in user's profile
    @GetMapping("/profile")
    public User userProfile(Principal principal) {
        String email = principal.getName();
        return userService.getUserByEmail(email);
    }

    // Create a new account (Personal or Business)
    @PostMapping("/account/create")
    public Account createAccount(@RequestParam String accountType, Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        return accountService.createAccountForUser(user, accountType);
    }

    // Retrieve all accounts for the logged-in user
    @GetMapping("/accounts")
    public List<Account> getAccounts(Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        return accountService.getAccountsForUser(user);
    }





}