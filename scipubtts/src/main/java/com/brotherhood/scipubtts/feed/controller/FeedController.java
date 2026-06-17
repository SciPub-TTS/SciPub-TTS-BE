package com.brotherhood.scipubtts.feed.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brotherhood.scipubtts.feed.dto.response.FeedResponse;
import com.brotherhood.scipubtts.feed.dto.response.FollowSummaryResponse;
import com.brotherhood.scipubtts.feed.dto.response.SuggestedTopicResponse;
import com.brotherhood.scipubtts.feed.service.FeedService;

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
    public List<FollowSummaryResponse> followedTopics() {
        return feedService.getFollowedTopics();
    }

    @GetMapping("/followed-authors")
    public List<FollowSummaryResponse> followedAuthors() {
        return feedService.getFollowedAuthors();
    }

    @GetMapping("/suggested-topics")
    public List<SuggestedTopicResponse> suggestedTopics() {
        return feedService.getSuggestedTopics();
    }

    @GetMapping("/sync")
    public ResponseEntity<ResponseObject> sync() {
        researchFeedSyncService.syncDailyFeed();

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(200, "Daily feed sync completed successfully", true)
        );
    }
}
