package com.brotherhood.scipubtts.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record RegisterLocalRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "First name is required")
        @Size(max = 255, message = "First name must not exceed 255 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 255, message = "Last name must not exceed 255 characters")
        String lastName,

        @NotBlank(message = "Password is required")
        String password,

        @NotBlank(message = "Confirm password is required")
        String confirmPassword,

        @URL(message = "Invalid app base URL")
        String appBaseUrl


) {
        @Override
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
