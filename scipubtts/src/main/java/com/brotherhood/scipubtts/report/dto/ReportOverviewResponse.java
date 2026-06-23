package com.brotherhood.scipubtts.report.dto;

import com.brotherhood.scipubtts.dashboard.dto.response.MetricsResponse;

import java.util.List;

public record ReportOverviewResponse(
        PublicationSummary publicationSummary,
        MetricsResponse metricSnapshot,
        List<TopicHighlight> trendingTopics,
        List<TopicHighlight> emergingTopics,
        List<TopicHighlight> impactTopics,
        List<KeywordHighlight> topKeywords
) {
  public record PublicationSummary(
          int totalPublications,
          double growthPercentage,
          String period
  ) {}

  public record TopicHighlight(
          String topicId,
          String name,
          Double score,
          Double change,
          String state
  ) {}

  public record KeywordHighlight(
          String keywordId,
          String name,
          Double score,
          Double change
  ) {}
}