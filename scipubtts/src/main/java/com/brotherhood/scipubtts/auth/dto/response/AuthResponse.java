package com.brotherhood.scipubtts.auth.dto.response;

import lombok.NonNull;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
    @Override
    @NonNull
    public String toString()
    {
        return "AuthResponse[" +
                "accessToken=***" +
                ", tokenType=" + tokenType +
                ", expiresInSeconds=" + expiresInSeconds +
                "]";
    }
}
