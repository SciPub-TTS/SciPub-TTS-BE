package com.brotherhood.scipubtts.feed.service;

import java.util.List;
import java.util.UUID;

import com.brotherhood.scipubtts.feed.controller.FeedTab;
import com.brotherhood.scipubtts.feed.dto.response.FeedResponse;
import com.brotherhood.scipubtts.feed.dto.response.FollowedTopicResponse;
import com.brotherhood.scipubtts.feed.dto.response.FollowedAuthorResponse;
import com.brotherhood.scipubtts.feed.dto.response.SuggestedTopicResponse;

public interface FeedService {
    FeedResponse getFeed(FeedTab feedTab, int page, int pageSize);

    List<FollowedTopicResponse> getFollowedTopics(UUID userId);

    List<FollowedAuthorResponse> getFollowedAuthors(UUID userId);

    List<SuggestedTopicResponse> getSuggestedTopics(UUID userId);
}
