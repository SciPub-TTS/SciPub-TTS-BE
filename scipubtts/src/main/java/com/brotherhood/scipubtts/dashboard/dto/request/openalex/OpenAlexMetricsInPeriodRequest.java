package com.brotherhood.scipubtts.dashboard.dto.request.openalex;

import java.util.List;

public record OpenAlexMetricsInPeriodRequest(
        String startTime,
        String endTime,
        List<Integer> fieldIds
) {
}