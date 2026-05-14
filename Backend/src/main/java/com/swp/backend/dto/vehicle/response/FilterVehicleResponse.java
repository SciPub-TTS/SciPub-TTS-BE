package com.swp.backend.dto.vehicle.response;

import java.util.UUID;

public record FilterVehicleResponse(
        UUID id,
        String  type,
        UUID rescueTeamId,
        String rescueTeamName
) {
}
