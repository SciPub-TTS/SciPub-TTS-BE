package com.brotherhood.scipubtts.feed.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brotherhood.scipubtts.common.annotation.CurrentUserUUID;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.feed.dto.response.FeedResponse;
import com.brotherhood.scipubtts.feed.dto.response.FollowedTopicResponse;
import com.brotherhood.scipubtts.feed.dto.response.FollowedAuthorResponse;
import com.brotherhood.scipubtts.feed.dto.response.SuggestedTopicResponse;
import com.brotherhood.scipubtts.feed.service.FeedService;
import com.brotherhood.scipubtts.feed.service.ResearchFeedSyncService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final ResearchFeedSyncService researchFeedSyncService;
    private final FeedService feedService;

    @GetMapping
    public FeedResponse getFeed(
            @RequestParam FeedTab feedTab,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return feedService.getFeed(feedTab, page, pageSize);
    }

    @GetMapping("/followed-topics")
    public List<FollowedTopicResponse> getFollowedTopics(@CurrentUserUUID UUID userId) {
        return feedService.getFollowedTopics(userId);
    }

    @GetMapping("/followed-authors")
    public List<FollowedAuthorResponse> followedAuthors(@CurrentUserUUID UUID userId) {
        return feedService.getFollowedAuthors(userId);
    }

    @GetMapping("/suggested-topics")
    public List<SuggestedTopicResponse> suggestedTopics(@CurrentUserUUID UUID userId) {
        return feedService.getSuggestedTopics(userId);
    }

    @GetMapping("/sync")
    public ResponseEntity<ResponseObject> sync() {
        researchFeedSyncService.syncDailyFeed();

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(200, "Daily feed sync completed successfully", true));
    }
}
