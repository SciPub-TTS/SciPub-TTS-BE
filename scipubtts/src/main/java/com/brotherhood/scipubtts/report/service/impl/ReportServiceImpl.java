package com.brotherhood.scipubtts.report.service.impl;

import com.brotherhood.scipubtts.dashboard.constant.FormulaType;
import com.brotherhood.scipubtts.dashboard.dto.request.KeywordRankingRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.PeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.SpecificTopicDataRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicDataRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.KeywordCalculateResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.MetricsResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.data.TopicRankingResponse;
import com.brotherhood.scipubtts.dashboard.service.DataService;
import com.brotherhood.scipubtts.dashboard.service.impl.KeywordServiceImpl;
import com.brotherhood.scipubtts.dashboard.service.impl.MetricServiceImpl;
import com.brotherhood.scipubtts.dashboard.service.impl.PublicationServiceImpl;
import com.brotherhood.scipubtts.report.dto.response.ReportOverviewResponse;
import com.brotherhood.scipubtts.report.dto.response.TopicFormulaComparisonResponse;
import com.brotherhood.scipubtts.report.dto.response.TopicTrendReportResponse;
import com.brotherhood.scipubtts.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

  private final PublicationServiceImpl publicationService;
  private final MetricServiceImpl metricService;
  private final KeywordServiceImpl keywordService;
  private final DataService dataService;

  @Override
  public ReportOverviewResponse getOverview(String startTime, String endTime, String fieldId) {

    var trends = publicationService.getPublicationTrendsFromDb();
    var pubSummary = buildPublicationSummary(trends);

    MetricsResponse metrics = metricService.getMetricsFromDb(new PeriodRequest(startTime, endTime));

    var trendingTopics = dataService.getTopicsRanking(
            new TopicDataRequest(startTime, endTime, fieldId, FormulaType.TRENDING.getFormula()));
    var emergingTopics = dataService.getTopicsRanking(
            new TopicDataRequest(startTime, endTime, fieldId, FormulaType.EMERGING.getFormula()));
    var impactTopics = dataService.getTopicsRanking(
            new TopicDataRequest(startTime, endTime, fieldId, FormulaType.IMPACT.getFormula()));

    var trendingKeywords = keywordService.getKeywordsRanking(
            new KeywordRankingRequest(LocalDate.parse(startTime), LocalDate.parse(endTime), fieldId, FormulaType.TRENDING.getFormula()));
    var emergingKeywords = keywordService.getKeywordsRanking(
            new KeywordRankingRequest(LocalDate.parse(startTime), LocalDate.parse(endTime), fieldId, FormulaType.EMERGING.getFormula()));
    var dominantKeywords = keywordService.getKeywordsRanking(
            new KeywordRankingRequest(LocalDate.parse(startTime), LocalDate.parse(endTime), fieldId, FormulaType.DOMINANT.getFormula()));

    return new ReportOverviewResponse(
            pubSummary,
            metrics,
            toTopicHighlights(trendingTopics, 5),
            toTopicHighlights(emergingTopics, 5),
            toTopicHighlights(impactTopics, 5),
            toKeywordHighlights(trendingKeywords, 5),
            toKeywordHighlights(emergingKeywords, 5),
            toKeywordHighlights(dominantKeywords, 5)
    );
  }

  @Override
  public TopicFormulaComparisonResponse compareTopicAcrossFormulas(
          String topicId, String fieldId, String startTime, String endTime) {

    Map<String, TopicFormulaComparisonResponse.FormulaResult> results = new LinkedHashMap<>();
    String name = null;

    for (FormulaType formula : FormulaType.values()) {
      if (formula == FormulaType.DOMINANT) continue; // Only applicable for keywords

      var ranking = dataService.getTopicsRanking(
              new TopicDataRequest(startTime, endTime, fieldId, formula.getFormula()));

      var topicData = ranking.topics().stream()
              .filter(t -> t.topicId().equals(topicId))
              .findFirst()
              .orElse(null);

      if (topicData != null) {
        name = topicData.name();
        int rank = ranking.topics().indexOf(topicData) + 1;
        results.put(formula.getFormula(), new TopicFormulaComparisonResponse.FormulaResult(
                rank, topicData.score(), topicData.change()));
      }
    }

    return new TopicFormulaComparisonResponse(topicId, name, results);
  }

  @Override
  public TopicTrendReportResponse getTopicTrendReport(
          String topicId, String fieldId, String startTime, String endTime) {

    var history = dataService.getTopicHeatMAp(
            new SpecificTopicDataRequest(startTime, endTime, fieldId));

    var topicHistory = history.topics().stream()
            .filter(t -> t.topicId().equals(topicId))
            .findFirst()
            .orElse(null);

    if (topicHistory == null || topicHistory.weeks().isEmpty()) {
      return new TopicTrendReportResponse(
              topicId, null, List.of(), "STABLE", null,
              "No historical data found for this topic within the selected time frame.");
    }

    List<TopicTrendReportResponse.WeeklyPoint> timeline = topicHistory.weeks().stream()
            .map(w -> new TopicTrendReportResponse.WeeklyPoint(
                    w.startDate(), w.endDate(),
                    w.velocity(), w.accelerate(), w.citationDecay(),
                    w.newComerAuthor(), w.institution()))
            .collect(Collectors.toList());

    double firstVelocity = topicHistory.weeks().get(0).velocity();
    double lastVelocity = topicHistory.weeks().get(topicHistory.weeks().size() - 1).velocity();
    double diff = lastVelocity - firstVelocity;

    String direction;
    if (diff > 0.05) direction = "RISING";
    else if (diff < -0.05) direction = "DECLINING";
    else direction = "STABLE";

    var peak = topicHistory.weeks().stream()
            .max(Comparator.comparingDouble(
                    com.brotherhood.scipubtts.dashboard.dto.response.data.SpecificTopicHistoryResponse.HistoryData::velocity))
            .orElse(null);
    String peakWeek = peak != null ? peak.startDate() + " - " + peak.endDate() : null;

    String insight = buildTrendInsight(topicHistory.name(), direction, peak, lastVelocity);

    return new TopicTrendReportResponse(
            topicId, topicHistory.name(), timeline, direction, peakWeek, insight);
  }

  private String buildTrendInsight(
          String name, String direction,
          com.brotherhood.scipubtts.dashboard.dto.response.data.SpecificTopicHistoryResponse.HistoryData peak,
          double lastVelocity) {

    String trendText = switch (direction) {
      case "RISING" -> "is gaining momentum";
      case "DECLINING" -> "is slowing down";
      default -> "remains stable";
    };

    String peakText = peak != null
            ? String.format(" The peak was recorded during the week of %s - %s with a velocity of %.2f.",
            peak.startDate(), peak.endDate(), peak.velocity())
            : "";

    return String.format("Topic \"%s\" %s, with a current velocity of %.2f.%s",
            name, trendText, lastVelocity, peakText);
  }

  // ---- helpers ----

  private ReportOverviewResponse.PublicationSummary buildPublicationSummary(Object trends) {
    // Calculate growth % based on the first and last points of trends — depending on the actual structure of PublicationServiceImpl
    return new ReportOverviewResponse.PublicationSummary(0, 0.0, "");
  }

  private List<ReportOverviewResponse.TopicHighlight> toTopicHighlights(
          TopicRankingResponse ranking, int limit) {
    return ranking.topics().stream()
            .limit(limit)
            .map(t -> new ReportOverviewResponse.TopicHighlight(
                    t.topicId(), t.name(), t.score(), t.change(), t.state().name()))
            .collect(Collectors.toList());
  }

  private List<ReportOverviewResponse.KeywordHighlight> toKeywordHighlights(
          KeywordCalculateResponse ranking, int limit) {
    return ranking.keywordList().stream()
            .sorted(Comparator.comparing(KeywordCalculateResponse.KeywordMetric::score).reversed())
            .limit(limit)
            .map(k -> new ReportOverviewResponse.KeywordHighlight(
                    k.keywordId(), k.name(), k.score(), k.cagr()))
            .collect(Collectors.toList());
  }
}