package com.brotherhood.scipubtts.dashboard.dto.response.data;

import java.util.List;

public record TopicRankingResponse(
        List<TopicData> topics
) {
  public record TopicData(
          String name,
          Integer works,
          Integer citations,
          Double score,
          Double change,
          String state,
          Boolean isFollowed
  ) {}
}