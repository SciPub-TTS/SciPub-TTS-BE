package com.brotherhood.scipubtts.feed.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // Enables safe cross-origin communication for development
public class FeedController {

    private final ResearchFeedSyncService researchFeedSyncService;
    private final FeedService feedService;

    @GetMapping
    @Operation(summary = "Get personalized feed publications", description = "Retrieves publications filtered by interest criteria and target tab categories.")
    public ResponseEntity<ResponseObject> getFeed(
            @RequestParam FeedTab feedTab,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        FeedResponse data = feedService.getFeed(feedTab, page, pageSize);

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Loaded feed publications", data));
    }

    @GetMapping("/followed-topics")
    @Operation(summary = "Get followed topics for user", description = "Returns the structured interest topics monitored by the authenticated user.")
    public ResponseEntity<ResponseObject> getFollowedTopics(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId) {

        List<FollowedTopicResponse> data = feedService.getFollowedTopics(userId);

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Loaded followed topics", data));
    }

    @GetMapping("/followed-authors")
    @Operation(summary = "Get followed authors for user", description = "Returns the research authors monitored by the authenticated user.")
    public ResponseEntity<ResponseObject> followedAuthors(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId) {

        List<FollowedAuthorResponse> data = feedService.getFollowedAuthors(userId);

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Loaded followed authors", data));
    }

    @GetMapping("/suggested-topics")
    @Operation(summary = "Get suggested trending topics", description = "Returns trending topic suggestions personalized for the user context.")
    public ResponseEntity<ResponseObject> suggestedTopics(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId) {

        List<SuggestedTopicResponse> data = feedService.getSuggestedTopics(userId);

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Loaded suggested topics", data));
    }

    @GetMapping("/sync")
    @Operation(summary = "Trigger daily publication synchronizations", description = "Performs ingestion pipelines across subscribed targets.")
    public ResponseEntity<ResponseObject> sync() {
        researchFeedSyncService.syncDailyFeed();

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(HttpStatus.OK.value(), "Daily feed sync completed successfully", true));
    }
}
