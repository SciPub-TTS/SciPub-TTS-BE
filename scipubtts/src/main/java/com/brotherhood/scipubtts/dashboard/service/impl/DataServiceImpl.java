package com.brotherhood.scipubtts.dashboard.service.impl;

import com.brotherhood.scipubtts.dashboard.dto.request.TopicRankingRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.TopicRankingResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.TopicScore;
import com.brotherhood.scipubtts.dashboard.entity.Topic;
import com.brotherhood.scipubtts.dashboard.repository.TopicRepository;
import com.brotherhood.scipubtts.dashboard.service.CalculationService;
import com.brotherhood.scipubtts.dashboard.service.DataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DataServiceImpl implements DataService {
  private final TopicRepository topicRepository;
  private final CalculationService calculationService;

  @Override
  public TopicRankingResponse getTopicsRanking(TopicRankingRequest request) {
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
}