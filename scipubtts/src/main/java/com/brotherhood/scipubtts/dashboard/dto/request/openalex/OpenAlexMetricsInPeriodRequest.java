package com.brotherhood.scipubtts.dashboard.dto.request.openalex;

public record OpenAlexMetricsInPeriodRequest(
        String startTime,
        String endTime
) {
}