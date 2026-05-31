package com.brotherhood.scipubtts.email.service;

public interface EmailService {
    public void sendVerificationEmail(String to, String verifyUrl);

}
