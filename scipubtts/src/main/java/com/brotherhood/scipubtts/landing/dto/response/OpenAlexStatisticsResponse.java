package com.brotherhood.scipubtts.landing.dto.response;

import lombok.Builder;

@Builder
public record OpenAlexStatisticsResponse(
        long totalPapers,
        long totalTopics,
        long totalAuthors,
        long totalFields
) {
}
