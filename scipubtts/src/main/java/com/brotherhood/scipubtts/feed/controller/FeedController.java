package com.brotherhood.scipubtts.feed.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brotherhood.scipubtts.feed.service.FeedService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public FeedResponse getFeed(
            @RequestParam FeedTab feedTab,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return feedService.getFeed(feedTab, page, pageSize);
    }

  }

    @GetMapping("/followed-topics")
    public List<FollowSummaryResponse> followedTopics() {
        return feedService.getFollowedTopics();
    }

    @GetMapping("/followed-topics")
    public List<FollowSummaryResponse> followedAuthors() {
        return feedService.getFollowedAuthors();
    }

    @GetMapping("/suggested-topics")
    public List<SuggestedTopicResponse> suggestedTopics() {
        return feedService.getSuggestedTopics();
    }

}
