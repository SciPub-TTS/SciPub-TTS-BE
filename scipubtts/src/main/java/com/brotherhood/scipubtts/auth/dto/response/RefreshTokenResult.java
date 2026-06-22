package com.brotherhood.scipubtts.auth.dto.response;

import com.brotherhood.scipubtts.user.entity.User;
import lombok.NonNull;

import java.time.OffsetDateTime;

public record RefreshTokenResult(
        User user,
        String rawToken,
        boolean rememberMe,
        OffsetDateTime expiresAt
) {
    @Override
    @NonNull
    public String toString() {
        return "RefreshTokenResult[" +
                "rawToken=***" +
                ", userId=" + user.getId() +
                ", rememberMe=" + rememberMe +
                ", expiresAt=" + expiresAt +
                "]";
    }
}