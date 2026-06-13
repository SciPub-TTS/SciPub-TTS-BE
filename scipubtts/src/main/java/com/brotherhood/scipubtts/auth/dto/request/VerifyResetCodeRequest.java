package com.brotherhood.scipubtts.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyResetCodeRequest(
        @Schema(example = "test@gmail.com", description = "Email address used in the reset request.")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(example = "123456", description = "6-digit verification code sent to the email.")
        @NotBlank(message = "Reset code is required")
        @Size(
                min = 6,
                max = 6,
                message = "Reset code must contain exactly 6 characters"
        )
        String code
) {
        @Override
        public String toString() {
                return "VerifyResetCodeRequest[" +
                        "email=" + email +
                        ", code=***" +
                        "]";
        }
}
