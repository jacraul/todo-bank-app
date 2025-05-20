package com.todobank.controller;

import com.todobank.entity.Account;
import com.todobank.entity.Transaction;
import com.todobank.entity.User;
import com.todobank.service.AccountService;
import com.todobank.service.TransactionService;
import com.todobank.service.UserService;
import com.todobank.util.AccountNumberGenerator;
import com.todobank.util.TransactionType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class HomeController {
    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    public HomeController(UserService userService, AccountService accountService, TransactionService transactionService) {
        this.userService = userService;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/requestAccount")
    public String requestAccount() {
        return "request";
    }

    @GetMapping("/otp-verify")
    public String otpVerify() {
        return "otp-verify";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        String firstName = user.getFirstName();
        request.setAttribute("firstName", firstName);

        List<Account> activeAccounts = accountService.getActiveAccountsForUser(user);
        request.setAttribute("accounts", activeAccounts);

        return "dashboard";
    }

    @GetMapping("/transfer")
    public String showTransferPage(Model model, Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        List<Account> activeAccounts = accountService.getActiveAccountsForUser(user);
        model.addAttribute("sourceAccounts", activeAccounts);
        return "transfer";
    }

    @GetMapping("/backoffice")
    public String backoffice() {
        return "admin";
    }

    @PostMapping("/transfer")
    public String handleTransfer(
            @RequestParam("sourceAccountNumber") String sourceAccountNumber,
            @RequestParam("targetAccountNumber") String targetAccountNumber,
            @RequestParam("amount") Double amount,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            String email = principal.getName();
            User user = userService.getUserByEmail(email);
            Account sourceAccount = accountService.getAccountByNumber(sourceAccountNumber);
            Account targetAccount = accountService.getAccountByNumber(targetAccountNumber);

            if (!sourceAccount.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized access to account");
            }

            if (targetAccount == null) {
                throw new RuntimeException("Target account not found");
            }

            if (amount <= 0) {
                throw new RuntimeException("Invalid transfer amount");
            }

            if (amount > sourceAccount.getBalance()) {
                throw new RuntimeException("Insufficient funds");
            }

            // Update account balances
            sourceAccount.setBalance(sourceAccount.getBalance() - amount);
            targetAccount.setBalance(targetAccount.getBalance() + amount);
            accountService.save(sourceAccount);
            accountService.save(targetAccount);

            // Create outgoing transaction
            Transaction outgoing = new Transaction();
            outgoing.setFromAccount(sourceAccount);
            outgoing.setToAccount(targetAccount);
            outgoing.setAmount(amount);
            outgoing.setTransactionType(TransactionType.TRANSFER_OUT);
            outgoing.setDescription("Transfer to account " + targetAccount.getAccountNumber());
            outgoing.setBalanceAfter(sourceAccount.getBalance());
            outgoing.setTransactionDate(LocalDateTime.now());
            transactionService.save(outgoing);

            // Create incoming transaction
            Transaction incoming = new Transaction();
            incoming.setFromAccount(targetAccount);
            incoming.setToAccount(sourceAccount);
            incoming.setAmount(amount);
            incoming.setTransactionType(TransactionType.TRANSFER_IN);
            incoming.setDescription("Transfer from account " + sourceAccount.getAccountNumber());
            incoming.setBalanceAfter(targetAccount.getBalance());
            incoming.setTransactionDate(LocalDateTime.now());
            transactionService.save(incoming);

            redirectAttributes.addFlashAttribute("successMessage", "Transfer completed successfully");
            return "redirect:/dashboard";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/transfer";
        }
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute User user, Model model) {
        userService.registerNewUser(user);
        model.addAttribute("successMessage", "Your account request has been submitted successfully!");
        return "register";
    }

    @GetMapping("/reports")
    public String showReports(Model model, Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        List<Account> activeAccounts = accountService.getActiveAccountsForUser(user);
        model.addAttribute("accounts", activeAccounts);
        return "reports";
    }

    @GetMapping("/reports/{accountNumber}")
    @ResponseBody
    public List<Transaction> getTransactions(@PathVariable String accountNumber, Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        Account account = accountService.getAccountByNumber(accountNumber);

        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to account");
        }

        return transactionService.getTransactionsForAccount(account);
    }

    @GetMapping("/accountdocument/{accountId}")
    public String getAccountDocument(@PathVariable Long accountId, Model model) {
        // Get the account details
        Account account = accountService.findById(accountId);
        if (account == null) {
            return "redirect:/dashboard";
        }

        // Get the user/client details
        User user = account.getUser(); // Assuming you have a relationship between Account and User

        // Add attributes to the model
        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("personalNumber", user.getPersonalNumber());
        model.addAttribute("accountNumber", account.getAccountNumber());
        model.addAttribute("status", account.getActive() ? "Active" : "Inactive");
        model.addAttribute("currentDate", LocalDateTime.now());

        return "accountdocument";
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        User user = userService.findByEmail(principal.getName());
        List<Account> accounts = accountService.findByUser(user);

        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        return "userprofile";
    }

    // Handle profile update
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String phone,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(principal.getName());
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhone(phone);

            userService.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile");
        }
        return "redirect:/profile";
    }

    // Handle password change
    @PostMapping("/profile/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(principal.getName());

            // Verify password confirmation
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match");
                return "redirect:/profile";
            }

            // Let UserService handle password validation and update
            boolean success = userService.updatePassword(user, currentPassword, newPassword);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to change password");
        }
        return "redirect:/profile";
    }

    // Handle new account request
    @PostMapping("/account/request")
    public String requestNewAccount(Principal principal, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(principal.getName());
            Account newAccount = new Account();
            newAccount.setUser(user);
            newAccount.setAccountNumber(AccountNumberGenerator.generateIBAN());
            newAccount.setBalance(0.0);
            newAccount.setActive(false); // Requires admin approval
            newAccount.setAccountType("CHECKING");

            accountService.save(newAccount);
            redirectAttributes.addFlashAttribute("successMessage", "Account request submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to submit account request");
        }
        return "redirect:/profile";
    }

}