package com.brotherhood.scipubtts.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NonNull;

public record RegisterLocalRequest(

        @Schema(example = "test@gmail.com", description = "User email address.")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(example = "Dang", description = "User first name.")
        @NotBlank(message = "First name is required")
        @Size(max = 255, message = "First name must not exceed 255 characters")
        String firstName,

        @Schema(example = "Nguyen", description = "User last name.")
        @NotBlank(message = "Last name is required")
        @Size(max = 255, message = "Last name must not exceed 255 characters")
        String lastName,

        @Schema(example = "123456", description = "User password.")
        @NotBlank(message = "Password is required")
        String password,

        @Schema(example = "123456", description = "Must match the password field.")
        @NotBlank(message = "Confirm password is required")
        String confirmPassword,

        @Schema(example = "http://localhost:5173", description = "Frontend base URL used to build the email verification redirect.")
        String appBaseUrl


) {
        @Override
        @NonNull
        public String toString() {
                return "RegisterLocalRequest[" +
                        "email=" + email +
                        ", firstName=" + firstName +
                        ", lastName=" + lastName +
                        ", password=***" +
                        ", confirmPassword=***" +
                        "]\", appBaseUrl=\" + appBaseUrl";
        }
}
