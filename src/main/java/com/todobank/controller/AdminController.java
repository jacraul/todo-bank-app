package com.todobank.controller;

import com.todobank.entity.Account;
import com.todobank.entity.User;
import com.todobank.form.UserUpdateForm;
import com.todobank.service.AccountService;
import com.todobank.service.EmailService;
import com.todobank.service.TransactionService;
import com.todobank.service.UserService;
import com.todobank.util.PasswordGenerator;
import com.todobank.util.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")  // Base mapping for all admin routes
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final EmailService emailService;

    @Autowired
    public AdminController(UserService userService, AccountService accountService, TransactionService transactionService, EmailService emailService) {
        this.userService = userService;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.emailService = emailService;
    }

    @GetMapping("")  // Maps to /admin
    public String adminPage(Model model) {
        List<User> inactiveUsers = userService.getInactiveUsers();
        model.addAttribute("inactiveUsers", inactiveUsers);
        return "admin";
    }

    @GetMapping("/requests/{userId}")  // Maps to /admin/requests/{userId}
    public String viewUserRequest(@PathVariable Long userId, Model model) {
        User user = userService.getUserById(userId);
        model.addAttribute("user", user);
        return "requests";
    }

    @PostMapping("/activate/{userId}")
    public String activateUser(@PathVariable Long userId) {
        userService.activateUser(userId);
        return "redirect:/admin/requests/" + userId;
    }

    @PostMapping("/deactivate/{userId}")
    public String deactivateUser(@PathVariable Long userId) {
        userService.deactivateUser(userId);
        return "redirect:/admin/requests/" + userId;
    }

    @GetMapping("/accounts")
    public String showAccountAdministration() {
        return "accountadministration";
    }

    @GetMapping("/accounts/search")
    public String searchAccounts(@RequestParam String personalNumber, Model model) {
        User user = userService.findByPersonalNumber(personalNumber);
        if (user != null) {
            List<Account> accounts = accountService.getAccountsForUser(user);
            model.addAttribute("accounts", accounts);
        } else {
            model.addAttribute("accounts", List.of());
        }
        return "accountadministration";
    }

    @PostMapping("/accounts/deactivate")
    public String deactivateAccount(@RequestParam String accountNumber,
                                    @RequestParam String personalNumber) {
        User user = userService.findByPersonalNumber(personalNumber);
        accountService.deleteAccount(accountNumber, user);
        return "redirect:/admin/accounts/search?personalNumber=" + personalNumber;
    }

    @PostMapping("/delete/{userId}")
    public String deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return "redirect:/admin";
    }

    @GetMapping("/cashoperations")
    public String showCashOperations() {
        return "cashoperations";
    }

    @PostMapping("/cashoperations")
    public String processCashOperation(
            @RequestParam("accountNumber") String accountNumber,
            @RequestParam("operationType") String operationType,
            @RequestParam("amount") Double amount,
            @RequestParam("details") String details,
            RedirectAttributes redirectAttributes) {
        try {
            Account account = accountService.getAccountByNumber(accountNumber);

            if (!account.getActive()) {
                throw new RuntimeException("Account is inactive");
            }

            if (amount <= 0) {
                throw new RuntimeException("Invalid amount");
            }

            if ("WITHDRAW".equals(operationType) && account.getBalance() < amount) {
                throw new RuntimeException("Insufficient funds");
            }

            Double newBalance;
            if ("DEPOSIT".equals(operationType)) {
                newBalance = account.getBalance() + amount;
            } else {
                newBalance = account.getBalance() - amount;
            }

            account.setBalance(newBalance);
            accountService.saveAccount(account);

            transactionService.saveTransaction(
                    account,
                    TransactionType.valueOf(operationType),
                    amount,
                    newBalance,
                    details
            );

            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("%s of %.2f RON completed successfully",
                            operationType.toLowerCase(), amount));

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/cashoperations";  // Fixed redirect path
    }

    @GetMapping("/clients")
    public String showClientsPage() {
        return "clients";
    }

    @PostMapping("/clients/search")
    public String searchClient(@RequestParam String personalNumber, Model model) {
        Optional<User> userOpt = Optional.ofNullable(userService.findByPersonalNumber(personalNumber));
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            List<Account> accounts = accountService.getAccountsForUser(user);
            model.addAttribute("client", user);
            model.addAttribute("accounts", accounts);
            return "clients";
        }
        model.addAttribute("error", "No client found with this personal number");
        return "clients";
    }

    @PostMapping("/clients/update")
    public String updateClient(@ModelAttribute UserUpdateForm form, RedirectAttributes redirectAttributes) {
        try {
            userService.updateClient(form);
            redirectAttributes.addFlashAttribute("message", "Client updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating client");
        }
        return "redirect:/admin/clients";
    }

    @PostMapping("/clients/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Starting password reset for user ID: " + id);
            String newPassword = PasswordGenerator.generateRandomPassword(12);
            System.out.println("Generated new password");

            userService.resetPassword(id, newPassword);
            System.out.println("Password reset completed");

            redirectAttributes.addFlashAttribute("message", "Password has been reset and sent to user's email");
            return "redirect:/admin/clients";

        } catch (Exception e) {
            System.err.println("Password reset failed: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to reset password: " + e.getMessage());
            return "redirect:/admin/clients";
        }
    }


    @PostMapping("/clients/{userId}/toggle-status")
    public String toggleStatus(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(userId);
            redirectAttributes.addFlashAttribute("message", "Client status updated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating status");
        }
        return "redirect:/admin/clients";
    }

    @PostMapping("/clients/{userId}/new-account")
    public String createNewAccount(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            accountService.createAccount(userId);
            redirectAttributes.addFlashAttribute("message", "New account created");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating account");
        }
        return "redirect:/admin/clients";
    }

    @PostMapping("/clients/{userId}/delete")
    public String deleteClient(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteClient(userId);
            redirectAttributes.addFlashAttribute("message", "Client deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting client");
        }
        return "redirect:/admin/clients";
    }
}