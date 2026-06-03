package com.brotherhood.scipubtts.user.dto.request;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword,
        String confirmNewPassword
) {}