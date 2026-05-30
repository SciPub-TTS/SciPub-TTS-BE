package com.brotherhood.scipubtts.auth.service;

public interface SecureValueService {
    public String generateOpaqueToken();

    public String sha256(String raw);

    public String generateOtpCode(int length);

}
