package com.brotherhood.scipubtts.auth.dto;

public record AuthResponse(
        String accessToken,
        String tokenType
) {}