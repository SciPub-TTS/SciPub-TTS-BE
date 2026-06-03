package com.brotherhood.scipubtts.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyResetCodeRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
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