package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.dashboard.dto.request.TopicCalculateAllRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.TopicCalculateResponse;
import com.brotherhood.scipubtts.dashboard.entity.Topic;

public interface TopicService {
  TopicCalculateResponse calculateAndSaveTopics(
          TopicCalculateAllRequest request
  );

  Topic calculateAndSaveTopic(
          String topicId,
          String startTime,
          String endTime,
          String fieldId
  );

  TopicCalculateResponse getTopicsFromDb(
          TopicCalculateAllRequest request
  );

  Topic getTopicFromDb(
          String topicId,
          String startTime,
          String endTime
  );
}