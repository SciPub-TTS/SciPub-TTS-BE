package com.brotherhood.scipubtts.email.service.impl;

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

//    @Override
//    public void sendPasswordResetSuccessNotice(String to) {
//        validateEmail(to);
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom(fromEmail);
//        message.setTo(to);
//        message.setSubject("Your SciPub-TTS password was changed");
//        message.setText(
//                "Your password has just been changed.\n\n" +
//                        "If this was not you, please contact support immediately."
//        );
//
//        mailSender.send(message);
//    }

    private void validateEmail(String to) {
        if (to == null || !to.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Invalid email address: " + to);
        }
    }
}


//@Service
//@RequiredArgsConstructor
//public class EmailServiceImpl implements EmailService {
//
//    private final JavaMailSender mailSender;
//
//    @Value("${spring.mail.username}")
//    private String fromEmail;
//
//    @Override
//    public void sendVerificationEmail(String to, String verifyLink) {
//        // validate basic email
//        if (to == null || !to.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
//            throw new RuntimeException("Invalid email address: " + to);
//        }
//
//        SimpleMailMessage message = new SimpleMailMessage();
//
//        message.setFrom(fromEmail);
//
//        // CHỈ dùng email FE gửi lên
//        message.setTo(to);
//
//        message.setSubject("Verify your SciPub-TTS account");
//
//        message.setText(
//                "Welcome to SciPub-TTS!\n\n" +
//                        "Please verify your account by clicking the link below:\n" +
//                        verifyLink + "\n\n" +
//                        "This link will expire in 24 hours."
//        );
//
//        mailSender.send(message);
//    }
//}