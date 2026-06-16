package com.brotherhood.scipubtts.feed.service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.brotherhood.scipubtts.feed.controller.FeedTab;
import com.brotherhood.scipubtts.feed.dto.response.FeedResponse;
import com.brotherhood.scipubtts.feed.dto.response.FollowSummaryResponse;
import com.brotherhood.scipubtts.feed.dto.response.SuggestedTopicResponse;
import com.brotherhood.scipubtts.feed.service.FeedService;

@Service
public class FeedServiceImpl implements FeedService {

  @Override
  public FeedResponse getFeed(
      FeedTab feedTab,
      int page,
      int pageSize) {
    return FeedResponse.builder()
        .items(List.of())
        .totalItems(0)
        .build();
  }

  @Override
  public List<FollowSummaryResponse> getFollowedTopics() {
    return List.of();
  }

  @Override
  public List<FollowSummaryResponse> getFollowedAuthors() {
    return List.of();
  }

  @Override
  public List<SuggestedTopicResponse> getSuggestedTopics() {
    return List.of();
  }
}
