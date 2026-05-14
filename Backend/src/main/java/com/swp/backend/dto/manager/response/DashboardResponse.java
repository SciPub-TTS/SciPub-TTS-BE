package com.swp.backend.dto.manager.response;

import java.util.List;

public record DashboardResponse(
        long totalRequests,
        double completionRate,
        long activeStaff,
        long totalStaff,
        long availableVehicle,
        long totalVehicle,
        List<TeamPerformance> topTeams,

        List<CityRequest> topCities
) {
    public record TeamPerformance(String leaderName, long completedCount) {}
    public record CityRequest(String city, long requestCount) {}
}