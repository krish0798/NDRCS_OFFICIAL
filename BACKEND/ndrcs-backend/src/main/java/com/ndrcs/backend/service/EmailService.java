package com.ndrcs.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${brevo.api.key:}")
    private String brevoApiKey;
    
    @Value("${brevo.sender.email:no-reply@ndrcs.com}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        String subject = "NDRCS Email Verification OTP";
        String htmlContent = "<html><body>" +
                "<p>Dear User,</p>" +
                "<p>Your OTP for NDRCS email verification is: <b>" + otpCode + "</b></p>" +
                "<p>This OTP will expire in 10 minutes.</p>" +
                "<p>Regards,<br>NDRCS Team</p>" +
                "</body></html>";
        String plainText = "Dear User,\n\nYour OTP is: " + otpCode + "\n\nRegards,\nNDRCS Team";

        // Route to Brevo HTTP API if API key is provided (e.g. Railway Deployment)
        if (brevoApiKey != null && !brevoApiKey.trim().isEmpty()) {
            sendViaBrevoApi(toEmail, subject, htmlContent);
        } else {
            // Fallback to local SMTP (e.g. Local Gmail Testing)
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(plainText);
            mailSender.send(message);
        }
    }

    private void sendViaBrevoApi(String toEmail, String subject, String htmlContent) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.brevo.com/v3/smtp/email";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            Map<String, Object> sender = new HashMap<>();
            sender.put("name", "NDRCS System");
            sender.put("email", senderEmail);

            Map<String, Object> recipient = new HashMap<>();
            recipient.put("email", toEmail);

            Map<String, Object> body = new HashMap<>();
            body.put("sender", sender);
            body.put("to", List.of(recipient));
            body.put("subject", subject);
            body.put("htmlContent", htmlContent);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            System.out.println("OTP successfully dispatched via Brevo REST API to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send email via Brevo REST API: " + e.getMessage());
            throw new RuntimeException("Email API failure");
        }
    }
}