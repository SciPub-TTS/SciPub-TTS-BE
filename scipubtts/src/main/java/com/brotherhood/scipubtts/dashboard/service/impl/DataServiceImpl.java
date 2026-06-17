package com.brotherhood.scipubtts.dashboard.service.impl;

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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DataServiceImpl implements DataService {
  private final TopicRepository topicRepository;
  private final CalculationService calculationService;
  private static final long PERIOD_DAYS = 7;
  private static final int HISTORY_WEEKS = 5;

  @Override
  public TopicRankingResponse getTopicsRanking(TopicDataRequest request) {
    LocalDate startDate = LocalDate.parse(request.startTime());
    LocalDate endDate   = LocalDate.parse(request.endTime());
    Integer   fieldId   = Integer.parseInt(request.fieldId());

    List<Topic> topics = topicRepository.findByStartTimeAndEndTimeAndFieldId(
            startDate, endDate, fieldId
    );

    if (topics.isEmpty()) return null;

    List<TopicScore> scores = calculationService.calculateTopicsFinalScore(
            request.formula(), topics
    );

    List<TopicRankingResponse.TopicData> topicDataList = scores.stream()
            .map(ts -> new TopicRankingResponse.TopicData(
                    ts.topic().getName(),
                    (int) ts.topic().getWorks(),
                    (int) ts.topic().getCitations(),
                    Math.round(ts.score() * 10000.0) / 100.0,
                    null,
                    null,
                    false
            ))
            .toList();

    return new TopicRankingResponse(topicDataList);
  }

  @Override
  public TopicMomentumResponse getTopicsMomentum(TopicDataRequest request) {
    LocalDate startDate = LocalDate.parse(request.startTime());
    LocalDate endDate   = LocalDate.parse(request.endTime());
    Integer   fieldId   = Integer.parseInt(request.fieldId());

    List<Topic> currentTopics = topicRepository.findByStartTimeAndEndTimeAndFieldId(
            startDate, endDate, fieldId
    );
    if (currentTopics.isEmpty()) return null;

    LocalDate pastStartDate = startDate.minusDays(PERIOD_DAYS);
    LocalDate pastEndDate = endDate.minusDays(PERIOD_DAYS);

    List<Topic> pastTopics = topicRepository.findByStartTimeAndEndTimeAndFieldId(
            pastStartDate, pastEndDate, fieldId
    );

    List<TopicScore> currentScores = calculationService.calculateTopicsFinalScore(request.formula(), currentTopics);

    // Map past topic scores for O(1) lookups
    Map<String, Double> pastScoreMap = Map.of();
    if (!pastTopics.isEmpty()) {
      pastScoreMap = pastTopics.stream().collect(Collectors.toMap(
              Topic::getTopicId,
              t -> calculationService.calculateTopicFinalScore(request.formula(), t.getTopicId(), pastTopics)
      ));
    }

    Map<String, Double> finalPastScoreMap = pastScoreMap;
    List<TopicMomentumResponse.Momentum> metrics = currentScores.stream()
            .map(ts -> {
              Topic topic = ts.topic();

              double currentAvg = Math.round(ts.score() * 10000.0) / 100.0;

              double rawPastScore = finalPastScoreMap.getOrDefault(topic.getTopicId(), 0D);
              double pastAvg = Math.round(rawPastScore * 10000.0) / 100.0;

              // Calculate growth percentage: ((Current - Past) / Past) * 100
              double growthPercentage = 0D;
              if (Double.compare(pastAvg, 0D) != 0) {
                double rawGrowth = ((currentAvg - pastAvg) / pastAvg) * 100.0;
                growthPercentage = Math.round(rawGrowth * 100.0) / 100.0;
              } else if (Double.compare(currentAvg, 0D) > 0) {
                growthPercentage = 100.0;
              }

              return new TopicMomentumResponse.Momentum(
                      topic.getName(),
                      currentAvg,
                      pastAvg,
                      growthPercentage
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
    int totalWeeks = HISTORY_WEEKS;
    List<WeekSnapshot> weekSnapshots = new ArrayList<>();

    for (int i = totalWeeks - 1; i >= 0; i--) {
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
}