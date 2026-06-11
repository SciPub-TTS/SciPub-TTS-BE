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

@Service
@RequiredArgsConstructor
public class MetricService {

    private static final long TOTAL_PAPER_PREVIOUS_PERIOD = 310854931L;
    private static final long TOTAL_TOPIC_PREVIOUS_PERIOD = 4406L;
    private static final long TOTAL_KEYWORD_PREVIOUS_PERIOD = 64904L;

    private final OpenAlexService openAlexService;

    public MetricsResponse takeMetrics(MetricRequest request) {
        long totalPapers = getEntityCount(OpenAlexEntity.WORKS);
        long totalTopics = getEntityCount(OpenAlexEntity.TOPICS);
        long totalKeywords = getEntityCount(OpenAlexEntity.KEYWORDS);

        long currentPeriodPapers = getPaperCountInPeriod(request.startTime(), request.endTime());
        long previousPeriodPapers = getPreviousPeriodPaperCount(request);

        List<Metric> metrics = List.of(
                new Metric(
                        MetricTitle.TOTAL_PAPERS.getTitle(),
                        totalPapers,
                        calculatePercentageChange(totalPapers, TOTAL_PAPER_PREVIOUS_PERIOD)
                ),
                new Metric(
                        MetricTitle.ACTIVE_TRENDING_TOPICS.getTitle(),
                        totalTopics,
                        calculatePercentageChange(totalTopics, TOTAL_TOPIC_PREVIOUS_PERIOD)
                ),
                new Metric(
                        MetricTitle.RISING_KEYWORDS.getTitle(),
                        totalKeywords,
                        calculatePercentageChange(totalKeywords, TOTAL_KEYWORD_PREVIOUS_PERIOD)
                ),
                new Metric(
                        MetricTitle.NEW_PAPERS_THIS_WEEK.getTitle(),
                        currentPeriodPapers,
                        calculatePercentageChange(currentPeriodPapers, previousPeriodPapers)
                )
        );

        return new MetricsResponse(metrics);
    }

    private long getEntityCount(OpenAlexEntity entity) {
        return openAlexService.takeMetricsToPeriod(new OpenAlexMetricsToPeriodRequest(entity))
                .meta()
                .count();
    }

    private long getPaperCountInPeriod(String startTime, String endTime) {
        return openAlexService.takeMetricsInPeriod(new OpenAlexMetricsInPeriodRequest(startTime, endTime))
                .meta()
                .count();
    }

    private long getPreviousPeriodPaperCount(MetricRequest request) {
        LocalDate startDate = LocalDate.parse(request.startTime());
        LocalDate endDate = LocalDate.parse(request.endTime());

        return getPaperCountInPeriod(
                startDate.minusWeeks(1).toString(),
                endDate.minusWeeks(1).toString()
        );
    }

    private double calculatePercentageChange(long currentValue, long previousValue) {
        if (previousValue == 0) {
            return 100.0;
        }

        return ((double) currentValue - previousValue) / previousValue * 100;
    }
}
