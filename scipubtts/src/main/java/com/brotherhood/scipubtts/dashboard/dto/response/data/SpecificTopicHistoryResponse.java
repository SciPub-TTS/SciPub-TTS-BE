package com.brotherhood.scipubtts.dashboard.dto.response.data;

import java.util.List;

public record SpecificTopicHistoryResponse(
        List<TopicHistory> topics
) {
  public record TopicHistory(
          String topicId,
          String name,
          List<HistoryData> weeks
  ) {}

  public record HistoryData(
          String startDate,
          String endDate,
          double velocity,
          double accelerate,
          double citationDecay,
          double newComerAuthor,
          double institution
  ) {}
}