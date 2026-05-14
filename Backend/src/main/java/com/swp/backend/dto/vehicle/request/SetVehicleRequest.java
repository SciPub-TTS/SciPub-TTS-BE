package com.swp.backend.dto.vehicle.request;

import java.util.UUID;

public record SetVehicleRequest(
        UUID id,
        String state
) {
}
