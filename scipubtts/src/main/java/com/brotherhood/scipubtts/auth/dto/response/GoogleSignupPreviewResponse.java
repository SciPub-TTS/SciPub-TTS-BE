package com.brotherhood.scipubtts.auth.dto.response;

public record GoogleSignupPreviewResponse(
        String email,
        String firstName,
        String lastName
) {
}
