package com.brotherhood.scipubtts.dashboard.dto.response.data;

import java.util.List;

public record TopicMomentumResponse(
        List<Momentum> topicGrowthMetrics
) {
  public record Momentum(
          String name,
          List<Point> history
  ) {}

  public record Point(
          String name,
          Double average
  ) {}
}