package com.swp.backend.dto.rescueTeam.response;

import com.swp.backend.dto.image.response.LookupImageResponse;

import java.util.List;
import java.util.UUID;

public record TaskDetailResponse (
        UUID requestId,
        UUID citizenId,
        String citizenName,
        String citizenPhone,
        String urgency,
        String address,
        Double latitude,
        Double longitude,
        String vehicleType,
        String description,
        String coordinatorName,
        String createdAt,
        String status,
        List<LookupImageResponse> images
){
}
