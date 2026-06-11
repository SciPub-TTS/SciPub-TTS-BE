package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.dashboard.dto.request.TopicCalculateAllRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicRankingRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.TopicCalculateResponse;
import com.brotherhood.scipubtts.dashboard.entity.Topic;

public interface TopicService {
  TopicCalculateResponse calculateAndSaveTopics(
          TopicCalculateAllRequest request
  );

  Topic calculateTopic(
          String topicId,
          String startTime,
          String endTime,
          String fieldId
  );

  TopicCalculateResponse getTopicsRanking(
          TopicRankingRequest request
  );

  Topic getTopicFromDb(
          String topicId,
          String startTime,
          String endTime
  );
}