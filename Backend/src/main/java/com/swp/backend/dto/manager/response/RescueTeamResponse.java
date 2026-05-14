package com.swp.backend.dto.manager.response;

import java.util.UUID;

public record RescueTeamResponse(
        UUID id,
        String leaderName,
        Integer teamSize,
        String phone,
        String staffState,
        long totalTasks
) {
}
