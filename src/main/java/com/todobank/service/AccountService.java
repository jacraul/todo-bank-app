package com.todobank.service;

import com.todobank.entity.Account;
import com.todobank.entity.User;
import com.todobank.repository.AccountRepository;
import com.todobank.util.AccountNumberGenerator;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final EmailService emailService;

    @Autowired
    public AccountService(AccountRepository accountRepository, EmailService emailService) {
        this.accountRepository = accountRepository;
        this.emailService = emailService;
    }

    public Account createAccountForUser(User user, String accountType) {
        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(AccountNumberGenerator.generateIBAN());
        account.setAccountType("CHECKING");
        account.setBalance(0.0);
        account.setActive(true);

        Account savedAccount = accountRepository.save(account);

        // Send email with account details
        String emailBody = String.format("""
            Dear %s %s,

            Your ToDo Bank account has been successfully activated.
            Your account details:
            Account Number: %s
            Account Type: %s

            You can now log in to your account using your email and previously provided password.
            Login URL: http://localhost:8080/login

            Best regards,
            ToDo Bank Team
            """,
                user.getFirstName(),
                user.getLastName(),
                savedAccount.getAccountNumber(),
                savedAccount.getAccountType()
        );

        emailService.sendEmail(user.getEmail(), "ToDo Bank Account Activated", emailBody);

        return savedAccount;
    }

    public void deleteAccount(String accountNumber, User user) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        // Verify account belongs to user
        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this account");
        }

        // Check if account has zero balance
        if (account.getBalance() > 0) {
            throw new RuntimeException("Cannot delete account with non-zero balance");
        }

        // Soft delete - mark as inactive
        account.setActive(false);
        accountRepository.save(account);

        // Send confirmation email asynchronously
        String emailBody = String.format("""
            Dear %s %s,

            Your ToDo Bank account has been successfully deactivated.
            Account Number: %s
            Account Type: %s

            If this was not intended, please contact our support team.

            Best regards,
            ToDo Bank Team
            """,
                user.getFirstName(),
                user.getLastName(),
                account.getAccountNumber(),
                account.getAccountType()
        );

        emailService.sendEmail(user.getEmail(), "ToDo Bank Account Deactivated", emailBody);
    }

    public List<Account> getAccountsForUser(User user) {
        return accountRepository.findByUser(user);
    }

    public List<Account> getActiveAccountsForUser(User user) {
        return accountRepository.findByUserAndActiveTrue(user);
    }

    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found with number: " + accountNumber));
    }

    public List<Account> getAccountsByUserEmail(String email) {
        return accountRepository.findByUserEmail(email);
    }

    public List<Account> getInactiveAccounts() {
        return accountRepository.findByActive(false);
    }

    @Transactional
    public void transfer(String sourceAccountNumber, String targetAccountNumber, Double amount) {
        Account sourceAccount = getAccountByNumber(sourceAccountNumber);
        Account targetAccount = getAccountByNumber(targetAccountNumber);

        if (!sourceAccount.getActive() || !targetAccount.getActive()) {
            throw new RuntimeException("One or both accounts are inactive");
        }

        if (sourceAccount.getBalance() < amount) {
            throw new RuntimeException("Insufficient funds");
        }

        sourceAccount.setBalance(sourceAccount.getBalance() - amount);
        targetAccount.setBalance(targetAccount.getBalance() + amount);

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        // Send email notifications
        notifyTransfer(sourceAccount, targetAccount, amount);
    }

    private void notifyTransfer(Account source, Account target, Double amount) {
        // Notify source account owner
        String sourceEmailBody = String.format(
                "Transfer completed: %.2f RON sent to account %s",
                amount, target.getAccountNumber()
        );
        emailService.sendEmail(source.getUser().getEmail(),
                "Transfer Notification", sourceEmailBody);

        // Notify target account owner
        String targetEmailBody = String.format(
                "Transfer received: %.2f RON from account %s",
                amount, source.getAccountNumber()
        );
        emailService.sendEmail(target.getUser().getEmail(),
                "Transfer Notification", targetEmailBody);
    }

    public void saveAccount(Account account) {
        accountRepository.save(account);
    }

    public void createAccount(Long userId) {
        User user = new User();
        user.setId(userId);
        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(AccountNumberGenerator.generateIBAN());
        account.setAccountType("CHECKING");
        account.setBalance(0.0);
        account.setActive(true);

        accountRepository.save(account);

        // Send email with account details
        String emailBody = String.format("""
            Dear %s,

            Your ToDo Bank account has been successfully created.
            Your account details:
            Account Number: %s
            Account Type: %s

            You can now log in to your account using your email and previously provided password.
            Login URL: http://localhost:8080/login

            Best regards,
            ToDo Bank Team
            """,
                user.getFirstName(),
                account.getAccountNumber(),
                account.getAccountType()
        );

        emailService.sendEmail(user.getEmail(), "ToDo Bank Account Created", emailBody);
    }

    public Account findById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
    }

    public void save(Account newAccount) {
        accountRepository.save(newAccount);
    }

    public List<Account> findByUser(User user) {
        return accountRepository.findByUser(user);
    }
}