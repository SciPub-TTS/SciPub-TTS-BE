package com.brotherhood.scipubtts.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteGoogleRegisterRequest(
        @NotBlank(message = "google Sign Up Token must not be blank")
        String googleSignupToken,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 10, max = 100, message = "Password must be between 10 and 100 characters")
        String password,

        @NotBlank(message = "Confirm Password must not be blank")
        @Size(min = 10, max = 100, message = "Confirm Password must be between 10 and 100 characters")
        String confirmPassword,

        Boolean rememberMe
) {
}
