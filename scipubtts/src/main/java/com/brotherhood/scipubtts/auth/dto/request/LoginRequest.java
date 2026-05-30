package com.brotherhood.scipubtts.auth.dto.request;

public record LoginRequest(
        String email,
        String password,
        boolean rememberMe
) {}
