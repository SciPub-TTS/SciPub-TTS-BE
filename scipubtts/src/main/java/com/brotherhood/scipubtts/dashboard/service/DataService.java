package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.dashboard.dto.request.TopicRankingRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.TopicRankingResponse;

public interface DataService {
  TopicRankingResponse getTopicsRanking(TopicRankingRequest request);
}