package com.brotherhood.scipubtts.dashboard.service.impl;

import com.brotherhood.scipubtts.dashboard.dto.request.TopicDataRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.data.TopicMomentumResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.data.TopicRankingResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.TopicScore;
import com.brotherhood.scipubtts.dashboard.entity.Topic;
import com.brotherhood.scipubtts.dashboard.repository.TopicRepository;
import com.brotherhood.scipubtts.dashboard.service.CalculationService;
import com.brotherhood.scipubtts.dashboard.service.DataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DataServiceImpl implements DataService {
  private final TopicRepository topicRepository;
  private final CalculationService calculationService;
  private static final long PERIOD_DAYS = 7;

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
}