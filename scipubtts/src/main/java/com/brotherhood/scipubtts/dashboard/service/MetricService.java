package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.dashboard.constant.MetricTitle;
import com.brotherhood.scipubtts.dashboard.constant.OpenAlexEntity;
import com.brotherhood.scipubtts.dashboard.dto.request.MetricRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.OpenAlexMetricsInPeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.OpenAlexMetricsToPeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.MetricsResponse;
import com.brotherhood.scipubtts.dashboard.entity.Metric;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MetricService {
  private final OpenAlexService openAlexService;

  public MetricsResponse takeMetrics(MetricRequest request){
    /// TODO: Query from db
    long totalPaperPreviousPeriod = 310854931L;
    long totalTopicPreviousPeriod = 4406L;
    long totalKeywordPreviousPeriod = 64904L;

    var totalPapersResponse = openAlexService.takeMetricsToPeriod(
            new OpenAlexMetricsToPeriodRequest(OpenAlexEntity.WORKS)
    );

    var totalTopicsResponse = openAlexService.takeMetricsToPeriod(
            new OpenAlexMetricsToPeriodRequest(OpenAlexEntity.TOPICS)
    );

    var totalKeywordsResponse = openAlexService.takeMetricsToPeriod(
            new OpenAlexMetricsToPeriodRequest(OpenAlexEntity.KEYWORDS)
    );

    var currentPeriodPapers =
            openAlexService.takeMetricsInPeriod(
                    new OpenAlexMetricsInPeriodRequest(
                            request.startTime(),
                            request.endTime()
                    )
            ).meta().count();

    var start = LocalDate.parse(request.startTime());
    var end = LocalDate.parse(request.endTime());

    var previousPeriodPapers =
            openAlexService.takeMetricsInPeriod(
                    new OpenAlexMetricsInPeriodRequest(
                            start.minusWeeks(1).toString(),
                            end.minusWeeks(1).toString()
                    )
            ).meta().count();

    long totalPapers = totalPapersResponse.meta().count();
    long totalTopics = totalTopicsResponse.meta().count();
    long totalKeywords = totalKeywordsResponse.meta().count();

    double totalPaperChange =
            ((double) totalPapers - totalPaperPreviousPeriod)/ totalPaperPreviousPeriod* 100;

    double totalTopicChange =
            ((double) totalTopics - totalTopicPreviousPeriod)/ totalTopicPreviousPeriod* 100;

    double totalKeywordChange =
            ((double) totalKeywords - totalKeywordPreviousPeriod)
                    / totalKeywordPreviousPeriod* 100;

    double currentPeriodChange = previousPeriodPapers == 0
            ? 100.0
            : ((double) currentPeriodPapers - previousPeriodPapers)
              / previousPeriodPapers * 100;

    List<Metric> metrics = List.of(
            new Metric(
                    MetricTitle.TOTAL_PAPERS.getTitle(),
                    totalPapers,
                    totalPaperChange
            ),
            new Metric(
                    MetricTitle.ACTIVE_TRENDING_TOPICS.getTitle(),
                    totalTopics,
                    totalTopicChange
            ),
            new Metric(
                    MetricTitle.RISING_KEYWORDS.getTitle(),
                    totalKeywords,
                    totalKeywordChange
            ),
            new Metric(
                    MetricTitle.NEW_PAPERS_THIS_WEEK.getTitle(),
                    currentPeriodPapers,
                    currentPeriodChange
            )
    );

    return new MetricsResponse(metrics);
  }
}