package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.dashboard.constant.MetricTitle;
import com.brotherhood.scipubtts.dashboard.constant.OpenAlexEntity;
import com.brotherhood.scipubtts.dashboard.dto.request.PeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexMetricsInPeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexMetricsToPeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.MetricsResponse;
import com.brotherhood.scipubtts.dashboard.entity.Metric;
import com.brotherhood.scipubtts.dashboard.repository.MetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MetricService {
  private final OpenAlexService openAlexService;
  private final MetricRepository metricRepository;

  private static final long PERIOD_DAYS = 14;

  // METHOD

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

  private MetricsResponse calculateMetrics(LocalDate startDate, LocalDate endDate, boolean calculatePrevious) {
    double totalPaperPreviousPeriod = calculatePrevious
            ? getPreviousMetricValue(MetricTitle.TOTAL_PAPERS.getTitle(), startDate, endDate, 310854931L) : 310854931L;

    double totalTopicPreviousPeriod = calculatePrevious
            ? getPreviousMetricValue(MetricTitle.ACTIVE_TRENDING_TOPICS.getTitle(), startDate, endDate, 4406L) : 4406L;

    double totalKeywordPreviousPeriod = calculatePrevious
            ? getPreviousMetricValue(MetricTitle.RISING_KEYWORDS.getTitle(), startDate, endDate, 64904L) : 64904L;

    long totalPapers = openAlexService.takeMetricsToPeriod(new OpenAlexMetricsToPeriodRequest(OpenAlexEntity.WORKS)).meta().count();
    long totalTopics = openAlexService.takeMetricsToPeriod(new OpenAlexMetricsToPeriodRequest(OpenAlexEntity.TOPICS)).meta().count();
    long totalKeywords = openAlexService.takeMetricsToPeriod(new OpenAlexMetricsToPeriodRequest(OpenAlexEntity.KEYWORDS)).meta().count();

    long currentPeriodPapers = openAlexService.takeMetricsInPeriod(new OpenAlexMetricsInPeriodRequest(startDate.toString(), endDate.toString())).meta().count();
    long previousPeriodPapers = openAlexService.takeMetricsInPeriod(new OpenAlexMetricsInPeriodRequest(startDate.minusDays(PERIOD_DAYS).toString(), endDate.minusDays(PERIOD_DAYS).toString())).meta().count();

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