package com.todobank.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("no.reply.todobank@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendOTPEmail(String email, String otpCode) {
        String subject = "Your OTP Code";
        String body = String.format("""
                Dear User,

                Your OTP code is: %s

                This code is valid for 5 minutes.

                Best regards,
                ToDo Bank Team
                """, otpCode);

        try {
            sendEmail(email, subject, body);
        } catch (MailException e) {
            System.err.println("Failed to send OTP email to " + email + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // src/main/java/com/todobank/service/EmailService.java

    public void sendPasswordResetEmail(String toEmail, String newPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("no.reply.todobank@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("ToDo Bank - Your Password Has Been Reset");

            String htmlContent = String.format("""
                <html>
                <body>
                    <h2>ToDo Bank - Password Reset</h2>
                    <p>Dear Customer,</p>
                    <p>Your password has been reset by an administrator.</p>
                    <p>Your new password is: <strong>%s</strong></p>
                    <p>Please log in and change your password immediately.</p>
                    <p>Best regards,<br>ToDo Bank Team</p>
                </body>
                </html>
                """, newPassword);

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}
