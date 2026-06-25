package com.brotherhood.scipubtts.report.controller;

import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/report")
public class ReportController {

  private final ReportService reportService;

  @GetMapping("/overview")
  public ResponseEntity<ResponseObject> getOverview(
          @RequestParam String startTime,
          @RequestParam String endTime,
          @RequestParam String fieldId
  ) {
    var data = reportService.getOverview(startTime, endTime, fieldId);
    return ResponseEntity.ok(new ResponseObject(
            HttpStatus.OK.value(), "Get report overview successfully", data));
  }

  @GetMapping("/topic-formula-comparison")
  public ResponseEntity<ResponseObject> compareTopicFormulas(
          @RequestParam String topicId,
          @RequestParam String fieldId,
          @RequestParam String startTime,
          @RequestParam String endTime
  ) {
    var data = reportService.compareTopicAcrossFormulas(topicId, fieldId, startTime, endTime);
    return ResponseEntity.ok(new ResponseObject(
            HttpStatus.OK.value(), "Compare topic across formulas successfully", data));
  }

  @GetMapping("/topic-trend")
  public ResponseEntity<ResponseObject> getTopicTrend(
          @RequestParam String topicId,
          @RequestParam String fieldId,
          @RequestParam String startTime,
          @RequestParam String endTime
  ) {
    var data = reportService.getTopicTrendReport(topicId, fieldId, startTime, endTime);
    return ResponseEntity.ok(new ResponseObject(
            HttpStatus.OK.value(), "Get topic trend report successfully", data));
  }
}