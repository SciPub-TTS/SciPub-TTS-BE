package com.brotherhood.scipubtts.dashboard.dto.response.data;

import com.brotherhood.scipubtts.dashboard.constant.TopicStatus;

import java.util.List;

public record TopicRankingResponse(
        List<TopicData> topics
) {
  public record TopicData(
          String name,
          String topicId,
          Integer works,
          Integer citations,
          Double score,
          Double change,
          TopicStatus state,
          Boolean isFollowed
  ) {}
}