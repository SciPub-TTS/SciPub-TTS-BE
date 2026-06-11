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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

  @PostMapping("/metrics")
  public ResponseEntity<ResponseObject> getMetrics(@Valid @RequestBody PeriodRequest request, HttpServletRequest httpServletRequest){
    var data = metricService.getMetricsFromDb(request);

    return ResponseEntity.ok(
            new ResponseObject(
                    HttpStatus.OK.value(),
                    "Get metrics successfully",
                    data
            )
    );
  }

  @PostMapping("/topicScore-all")
  public ResponseEntity<ResponseObject> getTopics(
          @Valid @RequestBody TopicRankingRequest request,
          HttpServletRequest httpServletRequest
  ) {

    var data = topicService.getTopicsRanking(request);

    return ResponseEntity.ok(
            new ResponseObject(
                    HttpStatus.OK.value(),
                    "Get topics successfully",
                    data
            )
    );
  }

  @PostMapping("/topicScore-single")
  public ResponseEntity<ResponseObject> getTopic(
          @Valid @RequestBody TopicCalculateSingleRequest request,
          HttpServletRequest httpServletRequest
  ) {

    var data = topicService.getTopicFromDb(
            request.topicId(),
            request.startTime(),
            request.endTime()
    );

    return ResponseEntity.ok(
            new ResponseObject(
                    HttpStatus.OK.value(),
                    "Get topic successfully",
                    data
            )
    );
  }

  @PostMapping("/keywordScore-all")
  public ResponseEntity<ResponseObject> getTopic(
          @Valid @RequestBody KeywordCalculateAllRequest request,
          HttpServletRequest httpServletRequest
  ){
    var data = keywordService.getByPeriod(request);
    return ResponseEntity.ok(
            new ResponseObject(
                    HttpStatus.OK.value(),
                    "Get keywords successfully",
                    data
            )
    );
  }
}