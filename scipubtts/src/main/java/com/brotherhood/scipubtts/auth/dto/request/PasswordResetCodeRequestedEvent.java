package com.brotherhood.scipubtts.auth.dto.request;

public record PasswordResetCodeRequestedEvent(
        String email,
        String code
) {
}