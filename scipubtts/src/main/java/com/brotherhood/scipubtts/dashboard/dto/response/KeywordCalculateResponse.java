package com.brotherhood.scipubtts.dashboard.dto.response;

import com.brotherhood.scipubtts.dashboard.entity.Keyword;

import java.time.LocalDate;
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