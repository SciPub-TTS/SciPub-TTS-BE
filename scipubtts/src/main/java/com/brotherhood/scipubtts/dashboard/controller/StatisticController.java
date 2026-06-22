package com.brotherhood.scipubtts.dashboard.controller;

import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.dashboard.dto.request.*;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexPublicationRequest;
import com.brotherhood.scipubtts.dashboard.service.impl.KeywordServiceImpl;
import com.brotherhood.scipubtts.dashboard.service.impl.MetricServiceImpl;
import com.brotherhood.scipubtts.dashboard.service.impl.PublicationServiceImpl;
import com.brotherhood.scipubtts.dashboard.service.impl.TopicServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/statistic")
public class StatisticController {
  private final PublicationServiceImpl publicationService;
  private final MetricServiceImpl metricService;
  private final TopicServiceImpl topicService;
  private final KeywordServiceImpl keywordService;

  @PostMapping("/publication-trends")
  public ResponseEntity<ResponseObject> takePublicationTrend(@Valid @RequestBody OpenAlexPublicationRequest request, HttpServletRequest httpServletRequest){
    var data = publicationService.calculateAndSavePublicationTrends(request);

    return ResponseEntity.ok(
            new ResponseObject(
                    HttpStatus.OK.value(),
                    "Get publication trends successfully",
                    data
            )
    );
  }

  @PostMapping("/metrics")
  public ResponseEntity<ResponseObject> takeMetrics(@Valid @RequestBody PeriodRequest request, HttpServletRequest httpServletRequest){
    var data = metricService.calculateAndSaveMetrics(request);

    return ResponseEntity.ok(
            new ResponseObject(
                    HttpStatus.OK.value(),
                    "Get publication trends successfully",
                    data
            )
    );
  }

  @PostMapping("/topicScore-all")
  public ResponseEntity<ResponseObject> calculateAllTopicsScore(
          @Valid @RequestBody TopicCalculateAllRequest request,
          HttpServletRequest httpServletRequest){
    long startTime = System.currentTimeMillis();

    var data = topicService.calculateAndSaveTopicsParallel(request);

    long duration = System.currentTimeMillis() - startTime;

    System.out.println(
            "calculateAllTopicsScore took "
                    + duration + " ms"
    );

    return ResponseEntity.ok(
            new ResponseObject(
                    HttpStatus.OK.value(),
                    "Calculate all topics score",
                    data
            )
    );
  }

  @PostMapping("/topicScore-single")
  public ResponseEntity<ResponseObject> calculateSingleTopicsScore(
          @Valid @RequestBody TopicCalculateSingleRequest request,
          HttpServletRequest httpServletRequest){
    long startTime = System.currentTimeMillis();

    var data = topicService.calculateTopic(
            request.topicId(),
            request.startTime(),
            request.endTime(),
            request.fieldId()
    );

    long duration = System.currentTimeMillis() - startTime;

    System.out.println(
            "calculateAllTopicsScore took "
                    + duration + " ms"
    );

    return ResponseEntity.ok(
            new ResponseObject(
                    HttpStatus.OK.value(),
                    "Calculate all topics score",
                    data
            )
    );
  }

  @PostMapping("/topicScore-previous-periods")
  public ResponseEntity<ResponseObject> calculateAllTopicsPreviousPeriods(
          @Valid @RequestBody TopicCalculateAllRequest request,
          HttpServletRequest httpServletRequest) {
    var data = topicService.calculateAllTopicsPreviousPeriodsParallel(request);

    return ResponseEntity.ok(
            new ResponseObject(
                    HttpStatus.OK.value(),
                    "Calculate previous periods for all topics",
                    data
            )
    );
  }

  @PostMapping("/keywordScore-all")
  public ResponseEntity<ResponseObject> calculateAllKeywordsScore(
          @Valid @RequestBody KeywordCalculateAllRequest request,
          HttpServletRequest httpServletRequest){
    var data = keywordService.calculateAndSaveKeywords(request);

    return ResponseEntity.ok(
            new ResponseObject(
                    HttpStatus.OK.value(),
                    "Calculate all keywords score",
                    data
            )
    );
  }
}