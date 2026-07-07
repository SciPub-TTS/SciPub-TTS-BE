package com.brotherhood.scipubtts.landing.dto.response;

import java.time.LocalDate;
import java.util.List;

public record LandingTrendPreviewResponse(
        LocalDate snapshotDate,
        long totalTrendingTopics,
        long totalTrendingKeywords,
        List<LandingTopicPreviewItemResponse> topTopics,
        List<LandingKeywordPreviewItemResponse> topKeywords
) {
}
