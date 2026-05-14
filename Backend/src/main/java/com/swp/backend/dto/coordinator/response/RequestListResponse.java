package com.swp.backend.dto.coordinator.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record RequestListResponse(
        UUID id,
        String citizenName,
        String phone,
        String status,
        LocalDateTime createdAt
) {
}
