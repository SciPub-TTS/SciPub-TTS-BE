package com.brotherhood.scipubtts.dashboard.service.impl;

import com.brotherhood.scipubtts.dashboard.constant.MetricTitle;
import com.brotherhood.scipubtts.dashboard.constant.OpenAlexEntity;
import com.brotherhood.scipubtts.dashboard.dto.request.PeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexMetricsInPeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexMetricsToPeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.MetricsResponse;
import com.brotherhood.scipubtts.dashboard.entity.Metric;
import com.brotherhood.scipubtts.dashboard.repository.MetricRepository;
import com.brotherhood.scipubtts.dashboard.service.MetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MetricServiceImpl implements MetricService {
  private final OpenAlexServiceImpl openAlexService;
  private final MetricRepository metricRepository;

  private static final long PERIOD_DAYS = 7;
  private static final List<Integer> TARGET_FIELDS = List.of(17, 22);

  private double calculateChange(double current, double previous) {
    if (previous == 0) {
      return current > 0 ? 100.0 : 0.0;
    }

    return (current - previous) / previous * 100;
  }

  private double getPreviousMetricValue(
          String title,
          LocalDate startTime,
          LocalDate endTime,
          double defaultValue
  ) {

    var previousStart = startTime.minusDays(PERIOD_DAYS);

    var previousEnd = endTime.minusDays(PERIOD_DAYS);

    return metricRepository
            .findByTitleAndStartTimeAndEndTime(
                    title,
                    previousStart,
                    previousEnd
            )
            .map(Metric::getValue)
            .orElse(defaultValue);
  }

  private void saveMetrics(MetricsResponse response, LocalDate startDate, LocalDate endDate) {
    for (var item : response.metricList()) {
      Metric metric = metricRepository.findByTitleAndStartTimeAndEndTime(item.title(), startDate, endDate)
              .orElseGet(Metric::new);

      metric.setTitle(item.title());
      metric.setValue(item.value());
      metric.setChange(item.change());
      metric.setStartTime(startDate);
      metric.setEndTime(endDate);

      metricRepository.save(metric);
    }
  }

  /**
   * Đếm tổng số bản ghi (works/topics) thuộc field 17|22, gọi 1 lần duy nhất bằng filter OR
   * để tránh đếm trùng các work thuộc cả 2 field.
   * KEYWORDS không hỗ trợ filter theo field trên OpenAlex -> lấy tổng toàn hệ thống.
   * WORKS được cộng thêm filter to_publication_date (lũy kế đến endDate).
   */
  private long countAcrossFields(OpenAlexEntity entity, LocalDate endDate) {
    if (entity == OpenAlexEntity.KEYWORDS) {
      var response = openAlexService.takeMetricsToPeriod(
              new OpenAlexMetricsToPeriodRequest(entity)
      );
      return response != null && response.meta() != null ? response.meta().count() : 0L;
    }

    boolean applyDateFilter = entity == OpenAlexEntity.WORKS;

    var request = applyDateFilter
            ? new OpenAlexMetricsToPeriodRequest(entity, TARGET_FIELDS, null, endDate.toString())
            : new OpenAlexMetricsToPeriodRequest(entity, TARGET_FIELDS);

    var response = openAlexService.takeMetricsToPeriod(request);
    return response != null && response.meta() != null ? response.meta().count() : 0L;
  }

  /**
   * Đếm số works xuất bản trong đúng khoảng startDate-endDate, thuộc field 17|22,
   * gọi 1 lần duy nhất bằng filter OR.
   */
  private long countInPeriodAcrossFields(String startDate, String endDate) {
    var response = openAlexService.takeMetricsInPeriod(
            new OpenAlexMetricsInPeriodRequest(startDate, endDate, TARGET_FIELDS)
    );
    return response != null && response.meta() != null ? response.meta().count() : 0L;
  }

  private MetricsResponse calculateMetrics(LocalDate startDate, LocalDate endDate, boolean calculatePrevious) {
    double totalPaperPreviousPeriod = calculatePrevious
            ? getPreviousMetricValue(MetricTitle.TOTAL_PAPERS.getTitle(), startDate, endDate, 310854931L) : 310854931L;

    double totalTopicPreviousPeriod = calculatePrevious
            ? getPreviousMetricValue(MetricTitle.ACTIVE_TRENDING_TOPICS.getTitle(), startDate, endDate, 4406L) : 4406L;

    double totalKeywordPreviousPeriod = calculatePrevious
            ? getPreviousMetricValue(MetricTitle.RISING_KEYWORDS.getTitle(), startDate, endDate, 64904L) : 64904L;

    long totalPapers = countAcrossFields(OpenAlexEntity.WORKS, endDate);
    long totalTopics = countAcrossFields(OpenAlexEntity.TOPICS, endDate);
    long totalKeywords = countAcrossFields(OpenAlexEntity.KEYWORDS, endDate);

    long currentPeriodPapers = countInPeriodAcrossFields(startDate.toString(), endDate.toString());
    long previousPeriodPapers = countInPeriodAcrossFields(
            startDate.minusDays(PERIOD_DAYS).toString(),
            endDate.minusDays(PERIOD_DAYS).toString()
    );

    return new MetricsResponse(List.of(
            new MetricsResponse.MetricItem(MetricTitle.TOTAL_PAPERS.getTitle(), totalPapers, calculateChange(totalPapers, totalPaperPreviousPeriod)),
            new MetricsResponse.MetricItem(MetricTitle.ACTIVE_TRENDING_TOPICS.getTitle(), totalTopics, calculateChange(totalTopics, totalTopicPreviousPeriod)),
            new MetricsResponse.MetricItem(MetricTitle.RISING_KEYWORDS.getTitle(), totalKeywords, calculateChange(totalKeywords, totalKeywordPreviousPeriod)),
            new MetricsResponse.MetricItem(MetricTitle.NEW_PAPERS_THIS_WEEK.getTitle(), currentPeriodPapers, calculateChange(currentPeriodPapers, previousPeriodPapers))
    ));
  }

  private double getOrCreatePreviousMetric(String title, LocalDate startDate, LocalDate endDate, double defaultValue) {
    LocalDate previousStart = startDate.minusDays(PERIOD_DAYS);
    LocalDate previousEnd = endDate.minusDays(PERIOD_DAYS);

    var metric = metricRepository.findByTitleAndStartTimeAndEndTime(title, previousStart, previousEnd);

    if (metric.isPresent()) {
      return metric.get().getValue();
    }

    if (!metricRepository.existsByStartTimeAndEndTime(previousStart, previousEnd)) {
      MetricsResponse previousResponse = calculateMetrics(previousStart, previousEnd, false);
      saveMetrics(previousResponse, previousStart, previousEnd);
    }

    return metricRepository.findByTitleAndStartTimeAndEndTime(title, previousStart, previousEnd)
            .map(Metric::getValue)
            .orElse(defaultValue);
  }

  public MetricsResponse calculateAndSaveMetrics(PeriodRequest request) {
    LocalDate startDate = LocalDate.parse(request.startTime());
    LocalDate endDate = LocalDate.parse(request.endTime());

    MetricsResponse response = calculateMetrics(startDate, endDate, true);

    saveMetrics(response, startDate, endDate);

    return response;
  }

  public MetricsResponse getMetricsFromDb(PeriodRequest request) {
    LocalDate startDate = LocalDate.parse(request.startTime());
    LocalDate endDate = LocalDate.parse(request.endTime());

    List<Metric> metrics = metricRepository.findByStartTimeAndEndTime(startDate, endDate);

    if (metrics.isEmpty()) {
      return calculateAndSaveMetrics(request);
    }

    return new MetricsResponse(
            metrics.stream()
                    .map(metric -> new MetricsResponse.MetricItem(
                            metric.getTitle(),
                            metric.getValue(),
                            metric.getChange()
                    ))
                    .toList()
    );
  }
}