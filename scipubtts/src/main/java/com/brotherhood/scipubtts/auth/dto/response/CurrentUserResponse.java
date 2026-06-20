package com.brotherhood.scipubtts.auth.dto.response;

import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String role,
        boolean googleLinked,
        String institution,
        String department,
        String country
) {}
