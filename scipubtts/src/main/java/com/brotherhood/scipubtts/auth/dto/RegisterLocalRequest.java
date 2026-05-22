package com.brotherhood.scipubtts.auth.dto;

public record RegisterLocalRequest(
        String email,
        String firstName,
        String lastName,
        String password,
        String appBaseUrl
) {
}
