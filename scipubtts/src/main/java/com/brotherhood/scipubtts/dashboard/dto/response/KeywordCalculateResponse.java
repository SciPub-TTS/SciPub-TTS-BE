package com.brotherhood.scipubtts.dashboard.dto.response;

import java.util.List;

public record KeywordCalculateResponse(
        List<KeywordMetric> keywordList
) {
  public record KeywordMetric(
          Long id,
          String keywordId,
          String name,
          String fieldId,
          Double score,
          Double cagr,
          Double ps,
          Long worksCount,
          Long citedByCount
  ){}
}