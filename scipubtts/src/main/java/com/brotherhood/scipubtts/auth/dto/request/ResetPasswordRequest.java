package com.brotherhood.scipubtts.auth.dto.request;

public record ResetPasswordRequest(
        String resetGrantToken,
        String newPassword,
        String confirmNewPassword
) {}