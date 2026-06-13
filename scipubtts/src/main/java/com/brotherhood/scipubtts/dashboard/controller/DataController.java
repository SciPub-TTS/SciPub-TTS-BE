package com.brotherhood.scipubtts.dashboard.controller;

import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.dashboard.dto.request.*;
import com.brotherhood.scipubtts.dashboard.service.impl.KeywordServiceImpl;
import com.brotherhood.scipubtts.dashboard.service.impl.MetricServiceImpl;
import com.brotherhood.scipubtts.dashboard.service.impl.PublicationServiceImpl;
import com.brotherhood.scipubtts.dashboard.service.impl.TopicServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/data")
public class DataController {
  private final PublicationServiceImpl publicationService;
  private final MetricServiceImpl metricService;
  private final TopicServiceImpl topicService;
  private final KeywordServiceImpl keywordService;

  @GetMapping("/publication-trends")
  public ResponseEntity<ResponseObject> getPublicationTrends() {

    var data = publicationService.getPublicationTrendsFromDb();

    return ResponseEntity.ok(
            new ResponseObject(
                    HttpStatus.OK.value(),
                    "Get publication trends successfully",
                    data
            )
    );
  }

  @GetMapping("/publication-trends/filter")
  public ResponseEntity<ResponseObject> getPublicationTrendsFromDb(
          @RequestParam Integer startYear,
          @RequestParam Integer endYear
  ) {

    var request = new PublicationTrendRequest(
            startYear,
            endYear
    );

    var data = publicationService.getPublicationTrendsFromDb(request);

    return ResponseEntity.ok(
            new ResponseObject(
                    HttpStatus.OK.value(),
                    "Get publication trends from database",
                    data
            )
    );
  }

  @GetMapping("/metrics")
  public ResponseEntity<ResponseObject> getMetrics(@Valid @RequestParam LocalDate startTime,
   @RequestParam LocalDate endTime ){

    PeriodRequest request = new PeriodRequest(
            startTime.toString(),
            endTime.toString()
    );
    var data = metricService.getMetricsFromDb(request);

    return ResponseEntity.ok(
            new ResponseObject(
                    HttpStatus.OK.value(),
                    "Get metrics successfully",
                    data
            )
    );
  }

  @GetMapping("/topicScore-all")
  public ResponseEntity<ResponseObject> getTopics(
          @RequestParam LocalDate startTime,
          @RequestParam Local endTime,
          @RequestParam String fieldId,
          @RequestParam String formula
  ) {

    var request = new TopicRankingRequest(
            startTime.toString(),
            endTime.toString(),
            fieldId,
            formula
    );

    var data = topicService.getTopicsRanking(request);

    return ResponseEntity.ok(
            new ResponseObject(
                    HttpStatus.OK.value(),
                    "Get topics successfully",
                    data
            )
    );
  }

  @GetMapping("/topicScore-single")
  public ResponseEntity<ResponseObject> getTopic(
          @RequestParam String topicId,
          @RequestParam LocalDate startTime,
          @RequestParam LocalDate endTime
  ) {

    var data = topicService.getTopicFromDb(
            topicId,
            startTime.toString(),
            endTime.toString()
    );

    return ResponseEntity.ok(
            new ResponseObject(
                    HttpStatus.OK.value(),
                    "Get topic successfully",
                    data
            )
    );
  }

  @GetMapping("/keywordScore-all")
  public ResponseEntity<ResponseObject> getKeywords(
          @RequestParam LocalDate startTime,
          @RequestParam LocalDate endTime,
          @RequestParam String fieldId,
          @RequestParam String formula
  ) {

    var request = new KeywordRankingRequest(
            startTime,
            endTime,
            fieldId,
            formula
    );

    var data = keywordService.getKeywordsRanking(request);

    return ResponseEntity.ok(
            new ResponseObject(
                    HttpStatus.OK.value(),
                    "Get keywords successfully",
                    data
            )
    );
  }
}