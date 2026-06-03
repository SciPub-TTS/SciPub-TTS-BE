package com.brotherhood.scipubtts.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "Reset grant token is required")
        String resetGrantToken,
        @NotBlank(message = "New password is required")
        String newPassword,
        @NotBlank(message = "Confirm new password is required")
        String confirmNewPassword
) {
        @Override
        public String toString()
        {
                return "ResetPasswordRequest[" +
                        "resetGrantToken=***" +
                        ", newPassword=***" +
                        ", confirmNewPassword=***" +
                        "]";
        }

}