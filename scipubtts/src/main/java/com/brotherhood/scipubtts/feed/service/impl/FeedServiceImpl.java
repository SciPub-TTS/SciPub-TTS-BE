package com.brotherhood.scipubtts.feed.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.brotherhood.scipubtts.feed.controller.FeedTab;
import com.brotherhood.scipubtts.feed.dto.response.FeedResponse;
import com.brotherhood.scipubtts.feed.dto.response.FollowedTopicResponse;
import com.brotherhood.scipubtts.feed.dto.response.FollowedAuthorResponse;
import com.brotherhood.scipubtts.feed.dto.response.SuggestedTopicResponse;
import com.brotherhood.scipubtts.feed.service.FeedService;
import com.brotherhood.scipubtts.follow.entity.FollowTargetType;
import com.brotherhood.scipubtts.follow.entity.UserFollow;
import com.brotherhood.scipubtts.follow.repository.UserFollowRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final UserFollowRepository userFollowRepository;

    @Override
    public FeedResponse getFeed(FeedTab feedTab, int page, int pageSize) {
        return FeedResponse.builder()
                .items(List.of())
                .totalItems(0)
                .build();
    }

    @Override
    public List<FollowedTopicResponse> getFollowedTopics(UUID userId) {
        List<UserFollow> follows = userFollowRepository.findByUserIdAndTargetType(userId, FollowTargetType.TOPIC);
        return follows.stream()
                .map(follow -> FollowedTopicResponse.builder()
                        .name(follow.getDisplayNameSnapshot() != null ? follow.getDisplayNameSnapshot()
                                : "Unknown Topic")
                        .status("Stable") // Default indicator for UI
                        .build())
                .toList();
    }

    @Override
    public List<FollowedAuthorResponse> getFollowedAuthors(UUID userId) {
        List<UserFollow> follows = userFollowRepository.findByUserIdAndTargetType(userId, FollowTargetType.AUTHOR);
        return follows.stream()
                .map(follow -> FollowedAuthorResponse.builder()
                        .name(follow.getDisplayNameSnapshot() != null ? follow.getDisplayNameSnapshot()
                                : "Unknown Author")
                        .field("Researcher") // Default fallback category for UI
                        .build())
                .toList();
    }

    @Override
    public List<SuggestedTopicResponse> getSuggestedTopics(UUID userId) {
        // Return static stub data for suggestions as currently required
        return List.of(
                SuggestedTopicResponse.builder().name("Academic Publishing and Open Access").build(),
                SuggestedTopicResponse.builder().name("AI Policy in Higher Education").build(),
                SuggestedTopicResponse.builder().name("Large Language Models").build());
    }
}
