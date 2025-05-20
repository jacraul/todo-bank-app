package com.todobank.service;

import com.todobank.entity.OTP;
import com.todobank.entity.User;
import com.todobank.repository.OTPRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OTPService {

    private OTPRepository otpRepository;
    private EmailService emailService;

    public OTPService(OTPRepository otpRepository, EmailService emailService) {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }

    public String generateOTP(User user) {
        Random random = new Random();
        String otpCode = String.format("%06d", random.nextInt(999999));

        OTP otp = new OTP();
        otp.setCode(otpCode);
        otp.setUser(user);
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        otpRepository.deleteByUser(user);
        otpRepository.save(otp);

        emailService.sendOTPEmail(user.getEmail(), otpCode);
        return otpCode;
    }

    public boolean verifyOTP(User user, String code) {
        OTP otp = otpRepository.findByUser(user).orElse(null);
        if(otp == null || otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        return otp.getCode().equals(code);
    }
}