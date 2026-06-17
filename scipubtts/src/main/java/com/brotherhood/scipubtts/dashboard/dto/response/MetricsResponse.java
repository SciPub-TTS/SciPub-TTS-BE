package com.brotherhood.scipubtts.dashboard.dto.response;


import java.util.List;

public record MetricsResponse(
        List<MetricItem> metricList
) {
    public record MetricItem(
            String title,
            double value,
            double change
    ) {
    }
}
