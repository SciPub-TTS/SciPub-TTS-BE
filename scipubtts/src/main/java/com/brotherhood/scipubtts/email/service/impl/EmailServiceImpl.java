package com.brotherhood.scipubtts.email.service.impl;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.email.service.EmailService;
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
        validateEmail(to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
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

    @Override
    public void sendPasswordResetCode(String to, String code) {
        validateEmail(to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Your SciPub-TTS password reset code");
        message.setText(
                "Your password reset code is: " + code + "\n\n" +
                        "This code will expire in 10 minutes.\n" +
                        "If you did not request this, you can ignore this email."
        );

        mailSender.send(message);
    }

    private void validateEmail(String to) {
        if (to == null || !to.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_FORMAT);
        }
    }

    @Override
    public void sendAccountBannedEmail(String to) {
        validateEmail(to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Important Notice: Your SciPub-TTS Account Status");
        message.setText(
                """
                        Hello,
                        
                        We are writing to inform you that your SciPub-TTS account has been temporarily suspended due to a violation of our Terms of Service or community guidelines.
                        
                        During this suspension, you will not be able to log in or access our services.
                        
                        If you believe this action was taken in error, please reply to this email or contact our support team for further assistance.
                        
                        Best regards,
                        The SciPub-TTS Team"""
        );

        mailSender.send(message);
    }

    @Override
    public void sendAccountUnbannedEmail(String to) {
        validateEmail(to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Update: Your SciPub-TTS Account has been Restored");
        message.setText(
                """
                        Hello,
                        
                        Good news! We have reviewed your account status and your SciPub-TTS account has been successfully restored.
                        
                        You can now log in and resume using all of our services normally.
                        
                        Thank you for your patience and for being a part of the SciPub-TTS community.
                        
                        Best regards,
                        The SciPub-TTS Team"""
        );

        mailSender.send(message);
    }
}