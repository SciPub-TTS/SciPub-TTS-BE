package com.brotherhood.scipubtts.email.service;

public interface EmailService {
    void sendVerificationEmail(String to, String verifyUrl);
    void sendPasswordResetCode(String to, String code);

    void sendAccountBannedEmail(String to);
    void sendAccountUnbannedEmail(String to);
}
