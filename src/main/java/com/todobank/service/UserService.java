package com.todobank.service;

import com.todobank.entity.User;
import com.todobank.form.UserUpdateForm;
import com.todobank.repository.UserRepository;
import com.todobank.util.PasswordGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AccountService accountService;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, AccountService accountService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.accountService = accountService;
    }

    public User registerNewUser(User user) {
        String rawPassword = PasswordGenerator.generateRandomPassword(8);
        user.setPassword(passwordEncoder.encode(rawPassword));

        HashSet<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        user.setRoles(roles);
        user.setActive(false);
        user.setVerified(false);
        user = userRepository.save(user);

        // Send registration email with password
        String emailBody = String.format("""
            Dear %s %s,

            Thank you for registering with ToDo Bank.
            Your account is pending activation by our admin team.

            Your login credentials:
            Email: %s
            Password: %s

            Please keep these credentials safe.
            You will be notified once your account is activated.

            Best regards,
            ToDo Bank Team
            """,
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                rawPassword
        );

        emailService.sendEmail(user.getEmail(), "ToDo Bank Registration Confirmation", emailBody);
        return user;
    }

    public User activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(true);
        user.setVerified(true);

        User savedUser = userRepository.save(user);


        String accountType = "PERSONAL";
        accountService.createAccountForUser(savedUser, accountType);

        return savedUser;
    }

    // Add the deactivateUser method
    public User deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        user.setVerified(false);
        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public List<User> getInactiveUsers() {
        return userRepository.findByActive(false);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User findByPersonalNumber(String personalNumber) {
        return userRepository.findByPersonalNumber(personalNumber)
                .orElse(null);  // Return null if not found
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);

        // Send account deletion email
        String emailBody = String.format("""
            Dear %s %s,

            Your account with ToDo Bank has been successfully deleted.

            If you have any questions, please contact our support team.

            Best regards,
            ToDo Bank Team
            """,
                user.getFirstName(),
                user.getLastName()
        );

        emailService.sendEmail(user.getEmail(), "ToDo Bank Account Deletion Confirmation", emailBody);
    }

    public void updateClient(UserUpdateForm form) {
        User user = userRepository.findById(form.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update user details
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setEmail(form.getEmail());
        user.setPhone(form.getPhone());

        userRepository.save(user);

        // Send account update email
        String emailBody = String.format("""
        Dear %s %s,

        Your account details have been updated to:
        - Email: %s
        - Phone: %s

        If you did not request this change, please contact us immediately.

        Best regards,
        ToDo Bank Team
        """,
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone()
        );

        emailService.sendEmail(user.getEmail(), "ToDo Bank Account Update Confirmation", emailBody);
    }

    public void resetPassword(Long userId, String newPassword) {
        try {
            User user = getUserById(userId);
            System.out.println("Resetting password for user: " + user.getEmail());

            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);
            User savedUser = userRepository.save(user);
            System.out.println("Password updated in database");

            String emailBody = String.format("""
            Dear %s %s,

            Your password has been reset.
            Your new password is: %s

            Please log in and change your password as soon as possible.

            Best regards,
            ToDo Bank Team
            """,
                    user.getFirstName(),
                    user.getLastName(),
                    newPassword  // Send the original password, not the encoded one
            );

            System.out.println("Sending password reset email");
            emailService.sendEmail(user.getEmail(), "ToDo Bank Password Reset", emailBody);
            System.out.println("Password reset email sent successfully");

        } catch (Exception e) {
            System.err.println("Error in resetPassword: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to reset password", e);
        }
    }


    public Optional<Object> findById(Long userId) {
        return Optional.of(userRepository.findById(userId));
    }

    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(!user.isActive());
        userRepository.save(user);

        String status = user.isActive() ? "activated" : "deactivated";

        // Send status update email with current status
        String emailBody = String.format("""
        Dear %s %s,

        Your account status has been updated.
        Current status: %s

        If you have any questions, please contact our support team.

        Best regards,
        ToDo Bank Team
        """,
                user.getFirstName(),
                user.getLastName(),
                status
        );

        emailService.sendEmail(user.getEmail(), "ToDo Bank Account Status Update Confirmation", emailBody);
    }

    public void deleteClient(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);

        // Send account deletion email
        String emailBody = String.format("""
            Dear %s %s,

            Your account with ToDo Bank has been successfully deleted.

            If you have any questions, please contact our support team.

            Best regards,
            ToDo Bank Team
            """,
                user.getFirstName(),
                user.getLastName()
        );

        emailService.sendEmail(user.getEmail(), "ToDo Bank Account Deletion Confirmation", emailBody);
    }

    public User findByEmail(String name) {
        return userRepository.findByEmail(name)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public boolean updatePassword(User user, String currentPassword, String newPassword) {
        // Check if the current password matches the stored password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false; // Current password is incorrect
        }

        // Update the password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true; // Password updated successfully
    }
}