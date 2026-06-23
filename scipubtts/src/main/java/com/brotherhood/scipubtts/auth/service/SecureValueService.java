package com.brotherhood.scipubtts.auth.service;

public interface SecureValueService {
    String generateOpaqueToken();

    String sha256(String raw);

    String generateOtpCode(int length);

}
