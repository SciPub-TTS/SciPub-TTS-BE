package com.brotherhood.scipubtts.admin.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String role,
        boolean emailVerified,
        boolean googleLinked,
        boolean banned,
        OffsetDateTime createdAt
) {
}
