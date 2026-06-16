package com.brotherhood.scipubtts.feed.controller;

import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.feed.service.ResearchFeedSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feed")
@RequiredArgsConstructor
public class FeedController {

    private final ResearchFeedSyncService researchFeedSyncService;

    @GetMapping("/sync")
    public ResponseEntity<ResponseObject> sync() {
        researchFeedSyncService.syncDailyFeed();

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(200, "Daily feed sync completed successfully", true)
        );
    }
}
