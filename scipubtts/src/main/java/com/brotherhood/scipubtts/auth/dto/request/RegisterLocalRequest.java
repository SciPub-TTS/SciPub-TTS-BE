package com.brotherhood.scipubtts.auth.dto.request;

public record RegisterLocalRequest(
        String email,
        String firstName,
        String lastName,
        String password,
        String confirmPassword,
        String appBaseUrl
) {
}
