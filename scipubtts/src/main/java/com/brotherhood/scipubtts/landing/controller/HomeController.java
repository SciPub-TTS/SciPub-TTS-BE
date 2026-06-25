package com.brotherhood.scipubtts.landing.controller;


import com.brotherhood.scipubtts.bookmark.service.BookmarkService;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.dashboard.dto.request.KeywordRankingRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicDataRequest;
import com.brotherhood.scipubtts.dashboard.service.KeywordService;
import com.brotherhood.scipubtts.dashboard.service.TopicService;
import com.brotherhood.scipubtts.landing.dto.response.HomeSummaryResponse;
import com.brotherhood.scipubtts.landing.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("api/home")
@RequiredArgsConstructor
public class HomeController {

    private final KeywordService keywordService;
    private final TopicService topicService;
    private final BookmarkService bookmarkService;
    private final HomeService homeService;

    @GetMapping("/landing/summary")
    public ResponseEntity<ResponseObject> getLandingSummary(
            @RequestParam LocalDate startTime,
            @RequestParam LocalDate endTime,
            @RequestParam String fieldId,
            @RequestParam String formula
    ) {
        // 1. Chuẩn bị Request Parameters cho Keyword và Topic
        KeywordRankingRequest kwReq = new KeywordRankingRequest(startTime, endTime, fieldId, formula);
        TopicDataRequest topicReq = new TopicDataRequest(startTime.toString(), endTime.toString(), fieldId, formula);

        // 2. Gọi các hàm bọc (Wrapper Methods)
        var top1Keyword = keywordService.getTop1KeywordRanking(kwReq);
        var top6Keywords = keywordService.getTop6KeywordsRanking(kwReq).keywordList();

        var top10Topics = topicService.getTopic10Ranking(topicReq);

        var top6Papers = bookmarkService.getTop6TrendingPapers();

        HomeSummaryResponse landingData = new HomeSummaryResponse(
                top1Keyword,
                top6Keywords,
                top10Topics.topics(),
                top6Papers
        );

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Successfully fetched landing dashboard data",
                        landingData
                )
        );
    }

    @GetMapping("/landing/statistics")
    public ResponseEntity<ResponseObject> getLandingStatistics() {
        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Statistics fetched successfully",
                        homeService.getStatistics()
                )
        );
    }
}
