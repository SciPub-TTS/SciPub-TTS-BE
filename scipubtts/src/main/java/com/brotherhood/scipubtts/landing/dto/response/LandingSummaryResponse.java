package com.brotherhood.scipubtts.landing.dto.response;

import com.brotherhood.scipubtts.bookmark.dto.response.TrendingPaperResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.KeywordCalculateResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.data.TopicRankingResponse;

import java.util.List;

public record LandingSummaryResponse(
        KeywordCalculateResponse.KeywordMetric top1Keyword,
        List<KeywordCalculateResponse.KeywordMetric> top6Keywords,
        List<TopicRankingResponse.TopicData> top10Topics,
        List<TrendingPaperResponse> top6TrendingPapers
) {
}
