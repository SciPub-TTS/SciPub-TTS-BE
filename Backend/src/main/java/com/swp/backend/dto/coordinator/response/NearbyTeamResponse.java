package com.swp.backend.dto.coordinator.response;

import java.util.UUID;

public record NearbyTeamResponse(
        UUID id,
        String teamName
) {
}
