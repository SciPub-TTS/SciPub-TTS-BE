package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.dashboard.constant.FormulaType;
import com.brotherhood.scipubtts.dashboard.dto.response.KeywordCalculateResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.TopicCalculateResponse;
import com.brotherhood.scipubtts.dashboard.entity.Keyword;
import com.brotherhood.scipubtts.dashboard.entity.Topic;

import java.util.List;

public interface CalculationService {

  TopicCalculateResponse calculateTopicsFinalScore(
          String formula, List<Topic> topicList
  );

  double calculateTopicFinalScore(
          String formula, String topicId, List<Topic> topicList
  );

  KeywordCalculateResponse calculateKeywordsFinalScore(
          String formula, List<Keyword> keywordList
  );

  double calculateKeywordFinalScore (
          String formula,String keywordId, List<Keyword> keywordList
  );
}