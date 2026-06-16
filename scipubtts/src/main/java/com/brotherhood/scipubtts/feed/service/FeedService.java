package com.brotherhood.scipubtts.feed.service;

import java.util.List;

import com.brotherhood.scipubtts.feed.controller.FeedTab;
import com.brotherhood.scipubtts.feed.dto.response.FeedResponse;
import com.brotherhood.scipubtts.feed.dto.response.FollowSummaryResponse;
import com.brotherhood.scipubtts.feed.dto.response.SuggestedTopicResponse;

public interface FeedService {
  FeedResponse getFeed(FeedTab feedTab, int page, int pageSize);

  List<FollowSummaryResponse> getFollowedTopics();

  List<FollowSummaryResponse> getFollowedAuthors();

  List<SuggestedTopicResponse> getSuggestedTopics();
}
