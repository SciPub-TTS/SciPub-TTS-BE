package com.brotherhood.scipubtts.schedule;

import com.brotherhood.scipubtts.dashboard.dto.request.KeywordCalculateAllRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.PeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicCalculateAllRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexPublicationRequest;
import com.brotherhood.scipubtts.dashboard.service.impl.KeywordServiceImpl;
import com.brotherhood.scipubtts.dashboard.service.impl.MetricServiceImpl;
import com.brotherhood.scipubtts.dashboard.service.impl.PublicationServiceImpl;
import com.brotherhood.scipubtts.dashboard.service.impl.TopicServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticWeeklyScheduler {

  private static final String KEYWORD_FORMULA = "balanced";

  private static final List<String> FIELD_IDS = List.of("17", "22");

  private final PublicationServiceImpl publicationService;
  private final MetricServiceImpl metricService;
  private final TopicServiceImpl topicService;
  private final KeywordServiceImpl keywordService;

  @Scheduled(cron = "0 30 0 * * MON")
  public void runWeeklyStatisticJob() {
    log.info("Bắt đầu job thống kê hàng tuần lúc 0h30 thứ 2");

    LocalDate now = LocalDate.now();
    LocalDate fiveYearsAgo = now.minusYears(5);

    runPublicationTrend(fiveYearsAgo, now);

    runMetrics(fiveYearsAgo, now);

    for (String fieldId : FIELD_IDS) {
      runTopicScoreAll(fieldId, fiveYearsAgo, now);
      runKeywordScoreAll(fieldId, fiveYearsAgo, now);
    }
  }

  private void runPublicationTrend(LocalDate startDate, LocalDate endDate) {
    try {
      var request = new OpenAlexPublicationRequest(
              String.valueOf(startDate.getYear()),
              String.valueOf(endDate.getYear())
      );
      publicationService.calculateAndSavePublicationTrends(request);
    } catch (Exception e) {
      log.error("Error in publication trends", e);
    }
  }

  private void runMetrics(LocalDate startDate, LocalDate endDate) {
    try {
      var request = new PeriodRequest(
              startDate.toString(),
              endDate.toString()
      );
      metricService.calculateAndSaveMetrics(request);
    } catch (Exception e) {
      log.error("Error in metrics", e);
    }
  }

  private void runTopicScoreAll(String fieldId, LocalDate startDate, LocalDate endDate) {
    try {
      var request = new TopicCalculateAllRequest(
              startDate.toString(),
              endDate.toString(),
              fieldId
      );
      topicService.calculateAndSaveTopicsParallel(request);
    } catch (Exception e) {
      log.error("Error in topic score fieldId={}", fieldId, e);
    }
  }

  private void runKeywordScoreAll(String fieldId, LocalDate startDate, LocalDate endDate) {
    try {
      var request = new KeywordCalculateAllRequest(
              startDate,
              endDate,
              fieldId,
              KEYWORD_FORMULA
      );
      keywordService.calculateAndSaveKeywords(request);
    } catch (Exception e) {
      log.error("Error in keyword score fieldId={}", fieldId, e);
    }
  }
}