package com.brotherhood.scipubtts.dashboard.dto.response.openalex;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenAlexMetricsResponse(
        Meta meta
) {
    public record Meta(
            Long count,

            @JsonProperty("cost_usd")
            Double costUsd
    ) {
    }
}
