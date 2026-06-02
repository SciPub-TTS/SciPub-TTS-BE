package com.brotherhood.scipubtts.auth.dto.response;

import com.brotherhood.scipubtts.user.entity.User;

import java.time.OffsetDateTime;

public record RefreshTokenResult(
        User user,
        String rawToken,
        boolean rememberMe,
        OffsetDateTime expiresAt
) {
    @Override
    public String toString() {
        return "RefreshTokenResult[" +
                "rawToken=***" +
                ", userId=" + user.getId() +
                ", rememberMe=" + rememberMe +
                ", expiresAt=" + expiresAt +
                "]";
    }
}