package com.swp.backend.dto.coordinator.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SpecificResponse(
        UUID id,
        String type,
        String description,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String additionalLink,
        String status,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        String urgency,
        UUID rescueTeamId,
        String rescueTeamName,
        UUID vehicleId,
        String vehicleType
) {
}
