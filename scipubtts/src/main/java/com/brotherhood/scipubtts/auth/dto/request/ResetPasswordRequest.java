package com.brotherhood.scipubtts.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

public record ResetPasswordRequest(
        @Schema(example = "reset_grant_token_example", description = "Reset token returned by the verify-code endpoint.")
        @NotBlank(message = "Reset grant token is required")
        String resetGrantToken,

        @Schema(example = "newPassword123", description = "New password to set.")
        @NotBlank(message = "New password is required")
        String newPassword,

        @Schema(example = "newPassword123", description = "Must match newPassword.")
        @NotBlank(message = "Confirm new password is required")
        String confirmNewPassword
) {
        @Override
        @NonNull
        public String toString()
        {
                return "ResetPasswordRequest[" +
                        "resetGrantToken=***" +
                        ", newPassword=***" +
                        ", confirmNewPassword=***" +
                        "]";
        }

}
