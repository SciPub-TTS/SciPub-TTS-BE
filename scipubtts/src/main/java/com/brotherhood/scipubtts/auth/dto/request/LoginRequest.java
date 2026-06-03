package com.brotherhood.scipubtts.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Password is required")
        String password,
        boolean rememberMe
) {
        @Override
        public String toString() {
                return "LoginRequest[" +
                        "email=" + email +
                        ", password=***" +
                        ", rememberMe=" + rememberMe +
                        "]";
        }
}
