package com.swp.backend.dto.manager.request;

import java.util.UUID;

public record CreateVehicleRequest(
        String type,
        UUID rescueTeamId,
        String state
) {
}
