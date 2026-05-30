package com.brotherhood.scipubtts.auth.dto.request;

public record VerifyResetCodeRequest(
        String email,
        String code
) {}