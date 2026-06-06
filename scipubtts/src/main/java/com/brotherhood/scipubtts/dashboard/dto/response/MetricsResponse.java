package com.brotherhood.scipubtts.dashboard.dto.response;

import com.brotherhood.scipubtts.dashboard.entity.Metric;

import java.util.List;

public record MetricsResponse(
        List<Metric> metricList
) {
}