package com.brotherhood.scipubtts.dashboard.dto.response.data;

import java.util.List;

public record SpecificTopicMetricDataResponse(
        MetricData average,
        List<MetricData> topics
) {
  public record MetricData(
          String name,
          double velocity,
          double accelerate,
          double citationDecay,
          double newComerAuthor,
          double institution
  ){}
}