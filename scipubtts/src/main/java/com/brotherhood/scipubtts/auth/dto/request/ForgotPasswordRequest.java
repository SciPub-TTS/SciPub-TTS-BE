package com.brotherhood.scipubtts.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @Schema(example = "test@gmail.com", description = "Email address of the account to recover.")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {}
