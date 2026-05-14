package com.swp.backend.dto.manager.request;

import java.util.UUID;

public record UpdateVehicleReqeust(
        String type,
        UUID rescueTeamId
) {
}
