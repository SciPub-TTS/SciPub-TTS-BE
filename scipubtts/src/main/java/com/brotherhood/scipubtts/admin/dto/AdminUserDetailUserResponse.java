package com.brotherhood.scipubtts.admin.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminUserDetailUserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String username,
        String avatarUrl,
        String role,
        boolean emailVerified,
        boolean googleLinked,
        boolean banned,
        OffsetDateTime createdAt
) {
}
