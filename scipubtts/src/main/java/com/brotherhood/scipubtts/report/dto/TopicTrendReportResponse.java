package com.brotherhood.scipubtts.report.dto;

import java.util.List;

public record TopicTrendReportResponse(
        String topicId,
        String name,
        List<WeeklyPoint> timeline,
        String trendDirection,   // "RISING" | "HOT" | "BREAKOUT"
        String peakWeek,
        String insight           // auto generate
) {
  public record WeeklyPoint(
          String startDate,
          String endDate,
          double velocity,
          double accelerate,
          double citationDecay
  ) {}
}