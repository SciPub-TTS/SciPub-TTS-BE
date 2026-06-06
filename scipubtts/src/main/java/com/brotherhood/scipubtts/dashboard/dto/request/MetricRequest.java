package com.brotherhood.scipubtts.dashboard.dto.request;

import com.brotherhood.scipubtts.dashboard.constant.OpenAlexEntity;

public record MetricRequest(
        String startTime,
        String endTime
) {
}