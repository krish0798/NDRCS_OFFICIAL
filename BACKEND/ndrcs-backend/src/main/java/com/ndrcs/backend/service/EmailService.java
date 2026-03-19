package com.ndrcs.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("NDRCS Email Verification OTP");
        message.setText(
                "Dear User,\n\n" +
                "Your OTP for NDRCS email verification is: " + otpCode + "\n\n" +
                "This OTP will expire in 10 minutes.\n\n" +
                "Regards,\nNDRCS Team"
        );

        mailSender.send(message);
    }
}