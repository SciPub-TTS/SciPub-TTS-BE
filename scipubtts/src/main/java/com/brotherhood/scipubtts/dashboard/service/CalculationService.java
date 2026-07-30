package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.dashboard.dto.response.KeywordCalculateResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.TopicScore;
import com.brotherhood.scipubtts.dashboard.entity.Keyword;
import com.brotherhood.scipubtts.dashboard.entity.Topic;
import com.brotherhood.scipubtts.dashboard.statistic.TopicMetricStatistic;

import java.util.List;

public interface CalculationService {

  List<TopicScore> calculateTopicsFinalScore(
          String formula, List<Topic> topicList
  );

  double calculateTopicFinalScore(
          String formula, String topicId, List<Topic> topicList
  );

  KeywordCalculateResponse calculateKeywordsFinalScore(
          String formula, List<Keyword> keywordList
  );




  TopicMetricStatistic buildTopicMetricStatistic(List<Topic> topics);

  double toNormalizedPercent(double value, double min, double max);
}