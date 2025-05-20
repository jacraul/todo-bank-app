package com.todobank.controller;

import com.todobank.entity.User;
import com.todobank.service.OTPService;
import com.todobank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private OTPService otpService;

    // Registration endpoint
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        // Here, the UserService sets a random password, default roles, and marks the user inactive until admin approval.
        return userService.registerNewUser(user);
    }

    // Endpoint to generate an OTP for MFA
    @PostMapping("/generate-otp")
    public String generateOtp(@RequestParam String email) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found.");
        }
        return otpService.generateOTP(user);
    }

    // Endpoint to verify the OTP
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email, @RequestParam String otpCode) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found.");
        }
        boolean verified = otpService.verifyOTP(user, otpCode);
        return verified ? "OTP verified successfully" : "OTP invalid or expired";
    }
}