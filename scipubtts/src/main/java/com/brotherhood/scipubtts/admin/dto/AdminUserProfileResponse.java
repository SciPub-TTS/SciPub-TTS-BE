package com.brotherhood.scipubtts.admin.dto;

import java.time.OffsetDateTime;

public record AdminUserProfileResponse(
        String institution,
        String department,
        String country,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
