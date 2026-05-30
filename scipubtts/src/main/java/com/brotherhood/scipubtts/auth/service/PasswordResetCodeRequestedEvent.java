package com.brotherhood.scipubtts.auth.service;

public record PasswordResetCodeRequestedEvent(
        String email,
        String code
) {
}
