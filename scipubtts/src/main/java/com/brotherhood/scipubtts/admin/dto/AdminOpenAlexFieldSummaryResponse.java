package com.brotherhood.scipubtts.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminOpenAlexFieldSummaryResponse(
        @JsonProperty("total_subfields")
        AdminDashboardStatisticsResponse.StatisticCard<Long> totalSubfields,

        @JsonProperty("total_topics")
        AdminDashboardStatisticsResponse.StatisticCard<Long> totalTopics
) {
}
