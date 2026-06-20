package com.brotherhood.scipubtts.auth.dto.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
    @Override
    public String toString()
    {
        return "AuthResponse[" +
                "accessToken=***" +
                ", tokenType=" + tokenType +
                ", expiresInSeconds=" + expiresInSeconds +
                "]";
    }
}
