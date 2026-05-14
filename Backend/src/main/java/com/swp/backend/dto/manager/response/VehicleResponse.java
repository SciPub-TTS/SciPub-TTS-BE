package com.swp.backend.dto.manager.response;

import java.util.UUID;

public record VehicleResponse(
    UUID id,
    String type,
    UUID ownerId,
    String team_owner,
    String state
) {
}
