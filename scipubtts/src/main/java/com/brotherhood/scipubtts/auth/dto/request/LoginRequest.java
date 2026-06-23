package com.brotherhood.scipubtts.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

public record LoginRequest(
        @Schema(example = "test@gmail.com", description = "User email address.")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(example = "123456", description = "User password.")
        @NotBlank(message = "Password is required")
        String password,

        @Schema(example = "true", description = "Whether the session should be remembered for a longer period.")
        boolean rememberMe
) {
        @Override
        @NonNull
        public String toString() {
                return "LoginRequest[" +
                        "email=" + email +
                        ", password=***" +
                        ", rememberMe=" + rememberMe +
                        "]";
        }
}
