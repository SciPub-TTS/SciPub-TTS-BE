package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.dashboard.dto.request.TopicCalculateAllRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicCalculateSingleRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicDataRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.TopicCalculateResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.data.TopicRankingResponse;
import com.brotherhood.scipubtts.dashboard.entity.Topic;
import com.brotherhood.scipubtts.feed.dto.response.SuggestedTopicResponse;

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
          TopicDataRequest request
  );

  TopicRankingResponse getTopic10Ranking(
          TopicDataRequest request
  );

  SuggestedTopicResponse getSuggestTopic(TopicDataRequest request);

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