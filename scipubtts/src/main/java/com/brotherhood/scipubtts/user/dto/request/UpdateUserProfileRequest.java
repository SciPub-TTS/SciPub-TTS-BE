package com.brotherhood.scipubtts.user.dto.request;

public record UpdateUserProfileRequest(
        String firstName,
        String lastName,
        String institution,
        String department,
        String country
) {}