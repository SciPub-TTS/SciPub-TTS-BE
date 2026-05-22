package com.brotherhood.scipubtts.email.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendVerificationEmail(String to, String verifyLink) {
        // validate basic email
        if (to == null || !to.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new RuntimeException("Invalid email address: " + to);
        }

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);

        // CHỈ dùng email FE gửi lên
        message.setTo(to);

        message.setSubject("Verify your SciPub-TTS account");

        message.setText(
                "Welcome to SciPub-TTS!\n\n" +
                        "Please verify your account by clicking the link below:\n" +
                        verifyLink + "\n\n" +
                        "This link will expire in 24 hours."
        );

        mailSender.send(message);
    }
}