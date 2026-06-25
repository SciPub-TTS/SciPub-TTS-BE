package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.dashboard.dto.request.KeywordCalculateAllRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.KeywordRankingRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.KeywordCalculateResponse;
import com.brotherhood.scipubtts.dashboard.entity.Keyword;

import java.util.List;

public interface KeywordService {

    KeywordCalculateResponse calculateAndSaveKeywords(
            KeywordCalculateAllRequest request
    );

    Keyword calculateKeywordMetrics(
            Keyword keyword,
            KeywordCalculateAllRequest request
    );

    List<Keyword> getByPeriod(
            KeywordCalculateAllRequest request
    );

    KeywordCalculateResponse getKeywordsRanking(
            KeywordRankingRequest request
    );


    KeywordCalculateResponse getTop6KeywordsRanking(KeywordRankingRequest request);

    KeywordCalculateResponse.KeywordMetric getTop1KeywordRanking(KeywordRankingRequest request);
}