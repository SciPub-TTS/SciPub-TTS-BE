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
import com.brotherhood.scipubtts.report.dto.ReportOverviewResponse;
import com.brotherhood.scipubtts.report.dto.TopicFormulaComparisonResponse;
import com.brotherhood.scipubtts.report.dto.TopicTrendReportResponse;
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

    var trending = dataService.getTopicsRanking(
            new TopicDataRequest(startTime, endTime, fieldId, FormulaType.TRENDING.getFormula()));
    var emerging = dataService.getTopicsRanking(
            new TopicDataRequest(startTime, endTime, fieldId, FormulaType.EMERGING.getFormula()));
    var impact = dataService.getTopicsRanking(
            new TopicDataRequest(startTime, endTime, fieldId, FormulaType.IMPACT.getFormula()));

    var keywords = keywordService.getKeywordsRanking(
            new KeywordRankingRequest(LocalDate.parse(startTime), LocalDate.parse(endTime), fieldId, FormulaType.DOMINANT.getFormula()));

    return new ReportOverviewResponse(
            pubSummary,
            metrics,
            toTopicHighlights(trending, 5),
            toTopicHighlights(emerging, 5),
            toTopicHighlights(impact, 5),
            toKeywordHighlights(keywords, 10)
    );
  }

  @Override
  public TopicFormulaComparisonResponse compareTopicAcrossFormulas(
          String topicId, String fieldId, String startTime, String endTime) {

    Map<String, TopicFormulaComparisonResponse.FormulaResult> results = new LinkedHashMap<>();
    String name = null;

    for (FormulaType formula : FormulaType.values()) {
      if (formula == FormulaType.DOMINANT) continue; // chỉ áp dụng cho keyword

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

    var history = dataService.getSpecificTopicMetric(
            new SpecificTopicDataRequest(startTime, endTime, fieldId));
    // Giả sử dùng API topicScore-periods (topicService.getTopicAcrossPeriods) để có timeline theo tuần
    var periods = dataService.getTopicsRanking(
            new TopicDataRequest(startTime, endTime, fieldId, FormulaType.BALANCED.getFormula()));

    // ... (lấy timeline thực tế từ TopicHistory nếu có topicId match)
    // Đoạn dưới minh hoạ logic tính insight, cần nối với dữ liệu thực tế của SpecificTopicHistoryResponse

    List<TopicTrendReportResponse.WeeklyPoint> timeline = new ArrayList<>();
    String direction = "STABLE";
    String peakWeek = null;
    String insight = "Không đủ dữ liệu để đánh giá xu hướng.";

    return new TopicTrendReportResponse(topicId, null, timeline, direction, peakWeek, insight);
  }

  // ---- helpers ----

  private ReportOverviewResponse.PublicationSummary buildPublicationSummary(Object trends) {
    // tính growth % dựa trên 2 điểm đầu/cuối của trends — tuỳ cấu trúc thực tế của PublicationServiceImpl
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