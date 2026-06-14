package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.dashboard.dto.request.TopicCalculateAllRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicCalculateSingleRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicRankingRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.TopicCalculateResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.TopicRankingResponse;
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

  TopicRankingResponse getTopicsRanking(
          TopicRankingRequest request
  );

  Topic getTopicFromDb(
          String topicId,
          String startTime,
          String endTime
  );

  TopicCalculateResponse calculateAllTopicsPreviousPeriods(
          TopicCalculateAllRequest request
  );

  TopicCalculateResponse getTopicAcrossPeriods(
          TopicCalculateSingleRequest request
  );
}