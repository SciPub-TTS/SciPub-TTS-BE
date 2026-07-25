package com.brotherhood.scipubtts.dashboard.service.impl;

import com.brotherhood.scipubtts.dashboard.constant.TopicStatus;
import com.brotherhood.scipubtts.dashboard.dto.request.SpecificTopicDataRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicDataRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.data.SpecificTopicHistoryResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.data.SpecificTopicMetricDataResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.data.TopicMomentumResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.data.TopicRankingResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.TopicScore;
import com.brotherhood.scipubtts.dashboard.entity.Topic;
import com.brotherhood.scipubtts.dashboard.repository.TopicRepository;
import com.brotherhood.scipubtts.dashboard.service.CalculationService;
import com.brotherhood.scipubtts.dashboard.service.DataService;
import com.brotherhood.scipubtts.dashboard.statistic.TopicMetricStatistic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DataServiceImpl implements DataService {
  private final TopicRepository topicRepository;
  private final CalculationService calculationService;
  private static final long PERIOD_DAYS = 7;
  private static final int HISTORY_WEEKS = 5;

  private ScoreComparison calculateScoreComparison(
          double rawCurrentScore,
          String topicId,
          Map<String, Double> pastScoreMap
  ) {

    double currentScore = Math.round(rawCurrentScore * 10000.0) / 100.0;

    double rawPastScore = pastScoreMap.getOrDefault(topicId, 0D);

    double pastScore = Math.round(rawPastScore * 10000.0) / 100.0;

    double change = 0D;

    if (Double.compare(pastScore, 0D) != 0) {
      double rawChange =
              ((currentScore - pastScore) / pastScore) * 100.0;

      change = Math.round(rawChange * 100.0) / 100.0;
    } else if (Double.compare(currentScore, 0D) > 0) {
      change = 100.0;
    }

    return new ScoreComparison(
            currentScore,
            pastScore,
            change
    );
  }

  private Map<String, Double> buildPastScoreMap(
          List<Topic> pastTopics,
          String formula
  ) {

    if (pastTopics.isEmpty()) {
      return Map.of();
    }

    return pastTopics.stream()
            .collect(Collectors.toMap(
                    Topic::getTopicId,
                    topic -> calculationService.calculateTopicFinalScore(
                            formula,
                            topic.getTopicId(),
                            pastTopics
                    )
            ));
  }

  // Helper record đóng gói kết quả trung gian
  private record TopicCalculationContext(
          List<TopicScore> currentScores,
          Map<String, Double> pastScoreMap
  ) {}

      private TopicCalculationContext prepareTopicCalculation(TopicDataRequest request) {
            LocalDate startDate = LocalDate.parse(request.startTime());
            LocalDate endDate   = LocalDate.parse(request.endTime());
            Integer fieldId     = Integer.parseInt(request.fieldId());

            List<Topic> currentTopics = topicRepository.findByStartTimeAndEndTimeAndFieldId(
                    startDate, endDate, fieldId
            );

            if (currentTopics.isEmpty()) {
              return new TopicCalculationContext(List.of(), Map.of());
            }

            LocalDate pastStartDate = startDate.minusDays(PERIOD_DAYS);
            LocalDate pastEndDate   = endDate.minusDays(PERIOD_DAYS);

            List<Topic> pastTopics = topicRepository.findByStartTimeAndEndTimeAndFieldId(
                    pastStartDate, pastEndDate, fieldId
            );

            List<TopicScore> currentScores = calculationService.calculateTopicsFinalScore(
                    request.formula(), currentTopics
            );

            Map<String, Double> pastScoreMap = buildPastScoreMap(pastTopics, request.formula());

            return new TopicCalculationContext(currentScores, pastScoreMap);
      }

  @Override
  public TopicRankingResponse getTopicsRanking(TopicDataRequest request) {
    TopicCalculationContext context = prepareTopicCalculation(request);

    if (context.currentScores().isEmpty()) {
      return new TopicRankingResponse(List.of());
    }

    List<TopicRankingResponse.TopicData> topicDataList = context.currentScores().stream()
            .map(ts -> {
              Topic topic = ts.topic();
              ScoreComparison comparison = calculateScoreComparison(
                      ts.score(),
                      topic.getTopicId(),
                      context.pastScoreMap()
              );

              return new TopicRankingResponse.TopicData(
                      topic.getName(),
                      topic.getTopicId(),
                      (int) topic.getWorks(),
                      (int) topic.getCitations(),
                      comparison.currentScore(),
                      comparison.change(),
                      TopicStatus.fromChange(comparison.change()),
                      false
              );
            })
            .toList();

    return new TopicRankingResponse(topicDataList);
  }

  @Override
  public TopicMomentumResponse getTopicsMomentum(TopicDataRequest request) {
    TopicCalculationContext context = prepareTopicCalculation(request);

    if (context.currentScores().isEmpty()) {
      return new TopicMomentumResponse(List.of());
    }

    List<TopicMomentumResponse.Momentum> metrics = context.currentScores().stream()
            .map(ts -> {
              Topic topic = ts.topic();
              ScoreComparison comparison = calculateScoreComparison(
                      ts.score(),
                      topic.getTopicId(),
                      context.pastScoreMap()
              );

              return new TopicMomentumResponse.Momentum(
                      topic.getName(),
                      comparison.currentScore(),
                      comparison.pastScore(),
                      comparison.change()
              );
            })
            .toList();

    return new TopicMomentumResponse(metrics);
  }

  @Override
  public SpecificTopicMetricDataResponse getSpecificTopicMetric(SpecificTopicDataRequest request){
    LocalDate startDate = LocalDate.parse(request.startTime());
    LocalDate endDate   = LocalDate.parse(request.endTime());
    Integer   fieldId   = Integer.parseInt(request.fieldId());

    List<Topic> currentTopics = topicRepository.findByStartTimeAndEndTimeAndFieldId(
            startDate, endDate, fieldId
    );
    if (currentTopics.isEmpty()) return null;

    TopicMetricStatistic stat = calculationService.buildTopicMetricStatistic(currentTopics);

    List<SpecificTopicMetricDataResponse.MetricData> topicMetrics = currentTopics.stream()
            .map(topic -> toNormalizedMetricData(topic, stat))
            .toList();

    SpecificTopicMetricDataResponse.MetricData average = new SpecificTopicMetricDataResponse.MetricData(
            "Average",
            round(topicMetrics.stream().mapToDouble(SpecificTopicMetricDataResponse.MetricData::velocity).average().orElse(0)),
            round(topicMetrics.stream().mapToDouble(SpecificTopicMetricDataResponse.MetricData::accelerate).average().orElse(0)),
            round(topicMetrics.stream().mapToDouble(SpecificTopicMetricDataResponse.MetricData::citationDecay).average().orElse(0)),
            round(topicMetrics.stream().mapToDouble(SpecificTopicMetricDataResponse.MetricData::newComerAuthor).average().orElse(0)),
            round(topicMetrics.stream().mapToDouble(SpecificTopicMetricDataResponse.MetricData::institution).average().orElse(0))
    );

    return new SpecificTopicMetricDataResponse(
            average,
            topicMetrics
    );
  }

  @Override
  public SpecificTopicHistoryResponse getTopicHeatMAp(SpecificTopicDataRequest request){
    LocalDate startDate = LocalDate.parse(request.startTime());
    LocalDate endDate   = LocalDate.parse(request.endTime());
    Integer   fieldId   = Integer.parseInt(request.fieldId());

    // Take topics list of current week
    List<Topic> baseTopics = topicRepository.findByStartTimeAndEndTimeAndFieldId(
            startDate, endDate, fieldId
    );
    if (baseTopics.isEmpty()) return new SpecificTopicHistoryResponse(List.of());

    Set<String> topicIds = baseTopics.stream()
            .map(Topic::getTopicId)
            .collect(Collectors.toSet());

    // Take 4 weeks ago and current week with normalize data
      List<WeekSnapshot> weekSnapshots = new ArrayList<>();

    for (int i = HISTORY_WEEKS - 1; i >= 0; i--) {
      long shiftDays = (long) i * PERIOD_DAYS;
      LocalDate wStart = startDate.minusDays(shiftDays);
      LocalDate wEnd   = endDate.minusDays(shiftDays);

      List<Topic> weekTopics = topicRepository.findByStartTimeAndEndTimeAndFieldId(
              wStart, wEnd, fieldId
      );

      // Just keep the topic occur in the based topics
      List<Topic> filtered = weekTopics.stream()
              .filter(t -> topicIds.contains(t.getTopicId()))
              .toList();

      TopicMetricStatistic stat = weekTopics.isEmpty()
              ? TopicMetricStatistic.empty()
              : calculationService.buildTopicMetricStatistic(weekTopics);

      weekSnapshots.add(new WeekSnapshot(wStart, wEnd, filtered, stat));
    }

    // Group by
    Map<String, List<SpecificTopicHistoryResponse.HistoryData>> historyMap = new LinkedHashMap<>();
    Map<String, String> nameMap = new LinkedHashMap<>();

    for (Topic t : baseTopics) {
      historyMap.put(t.getTopicId(), new ArrayList<>());
      nameMap.put(t.getTopicId(), t.getName());
    }

    for (WeekSnapshot snap : weekSnapshots) {
      Map<String, Topic> topicByIdThisWeek = snap.topics().stream()
              .collect(Collectors.toMap(Topic::getTopicId, t -> t, (a, b) -> a));

      for (String topicId : historyMap.keySet()) {
        Topic topic = topicByIdThisWeek.get(topicId);
        SpecificTopicHistoryResponse.HistoryData data;

        if (topic != null) {
          data = new SpecificTopicHistoryResponse.HistoryData(
                  snap.startDate().toString(),
                  snap.endDate().toString(),
                  calculationService.toNormalizedPercent(topic.getVelocity(),    snap.stat().velocityMin(),     snap.stat().velocityMax()),
                  calculationService.toNormalizedPercent(topic.getAcceleration(),snap.stat().accelerationMin(), snap.stat().accelerationMax()),
                  calculationService.toNormalizedPercent(topic.getCitationDecay(),snap.stat().citationMin(),    snap.stat().citationMax()),
                  calculationService.toNormalizedPercent(topic.getNewComerAuthor(),snap.stat().newcomerMin(),   snap.stat().newcomerMax()),
                  calculationService.toNormalizedPercent(topic.getInstitution(), snap.stat().institutionMin(),  snap.stat().institutionMax())
          );
        } else {
          // Return 0 if there is no data
          data = new SpecificTopicHistoryResponse.HistoryData(
                  snap.startDate().toString(),
                  snap.endDate().toString(),
                  0, 0, 0, 0, 0
          );
        }
        historyMap.get(topicId).add(data);
      }
    }

    // 4. Build response
    List<SpecificTopicHistoryResponse.TopicHistory> result = historyMap.entrySet().stream()
            .map(e -> new SpecificTopicHistoryResponse.TopicHistory(
                    e.getKey(),
                    nameMap.get(e.getKey()),
                    e.getValue()
            ))
            .toList();

    return new SpecificTopicHistoryResponse(result);
  }

  private SpecificTopicMetricDataResponse.MetricData toNormalizedMetricData(
          Topic topic,
          TopicMetricStatistic stat
  ) {
    return new SpecificTopicMetricDataResponse.MetricData(
            topic.getName(),
            calculationService.toNormalizedPercent(topic.getVelocity(), stat.velocityMin(), stat.velocityMax()),
            calculationService.toNormalizedPercent(topic.getAcceleration(), stat.accelerationMin(), stat.accelerationMax()),
            calculationService.toNormalizedPercent(topic.getCitationDecay(), stat.citationMin(), stat.citationMax()),
            calculationService.toNormalizedPercent(topic.getNewComerAuthor(), stat.newcomerMin(), stat.newcomerMax()),
            calculationService.toNormalizedPercent(topic.getInstitution(), stat.institutionMin(), stat.institutionMax())
    );
  }

  private double round(double value) {
    return Math.round(value * 100.0) / 100.0;
  }

  private record WeekSnapshot(
          LocalDate startDate,
          LocalDate endDate,
          List<Topic> topics,
          TopicMetricStatistic stat
  ) {}

  private record ScoreComparison(
          double currentScore,
          double pastScore,
          double change
  ) {}
}