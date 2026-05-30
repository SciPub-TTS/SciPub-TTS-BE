package com.brotherhood.scipubtts.auth.dto.response;

public record VerifyResetCodeResponse(
        String resetGrantToken,
        long expiresInSeconds
) {}