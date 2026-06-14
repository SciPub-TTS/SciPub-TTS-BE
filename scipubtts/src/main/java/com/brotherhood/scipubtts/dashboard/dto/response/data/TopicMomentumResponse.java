package com.brotherhood.scipubtts.dashboard.dto.response.data;

import java.util.List;

public record TopicMomentumResponse(
        List<Momentum> topicGrowthMetrics
) {
  public record Momentum(
          String name,
          Double currentAverage,
          Double pastAverage,
          Double growthPercentage
  ) {}
}