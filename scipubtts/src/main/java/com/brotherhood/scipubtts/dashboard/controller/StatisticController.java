package com.brotherhood.scipubtts.dashboard.controller;

import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.dashboard.dto.request.PeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicCalculateRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexPublicationRequest;
import com.brotherhood.scipubtts.dashboard.service.MetricService;
import com.brotherhood.scipubtts.dashboard.service.PublicationService;
import com.brotherhood.scipubtts.dashboard.service.TopicService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistic")
public class StatisticController {
  private final PublicationService publicationService;
  private final MetricService metricService;
  private final TopicService topicService;

  public StatisticController(PublicationService publicationService, MetricService metricService, TopicService topicService) {
    this.publicationService = publicationService;
    this.metricService = metricService;
    this.topicService = topicService;
  }

  @PostMapping("/publication-trends")
  public ResponseEntity<ResponseObject> takePublicationTrend(@Valid @RequestBody OpenAlexPublicationRequest request, HttpServletRequest httpServletRequest){
    var data = publicationService.takePublicationTrends(request);

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
    var data = metricService.takeMetrics(request);

    return ResponseEntity.ok(
            new ResponseObject(
                    HttpStatus.OK.value(),
                    "Get publication trends successfully",
                    data
            )
    );
  }

  @PostMapping("/TopicScore")
  public ResponseEntity<ResponseObject> calculateTopicsScore(
          @Valid @RequestBody TopicCalculateRequest request,
          HttpServletRequest httpServletRequest){
    var data = topicService.calculateVelocityAllTopics(request);

    // TODO: data will be calculated through more service to fill full the topic score.

    return ResponseEntity.ok(
            new ResponseObject(
                    HttpStatus.OK.value(),
                    "Calculate all topics score",
                    data
            )
    );
  }
}