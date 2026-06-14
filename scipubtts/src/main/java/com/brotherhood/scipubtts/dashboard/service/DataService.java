package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.dashboard.dto.request.TopicDataRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.data.TopicMomentumResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.data.TopicRankingResponse;

public interface DataService {
  TopicRankingResponse getTopicsRanking(TopicDataRequest request);

  TopicMomentumResponse getTopicsMomentum(TopicDataRequest request);
}