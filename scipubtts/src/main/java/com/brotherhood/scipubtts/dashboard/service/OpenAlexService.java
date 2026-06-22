package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.dashboard.dto.request.TopicHotFilterRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexMetricsInPeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexMetricsToPeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexPublicationRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexTopicFilterRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.KeywordHotFilterResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.TopicHotFilterResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.openalex.OpenAlexMetricsResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.openalex.OpenAlexPublicationResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.openalex.OpenAlexWorkCitationResponse;
import com.brotherhood.scipubtts.dashboard.entity.Topic;

import java.time.LocalDate;
import java.util.Set;

public interface OpenAlexService {
  // Publication
  OpenAlexPublicationResponse searchPublicationsByYear(
          OpenAlexPublicationRequest request
  );

  // Metrics
  OpenAlexMetricsResponse takeMetricsInPeriod(
          OpenAlexMetricsInPeriodRequest request
  );

  OpenAlexMetricsResponse takeMetricsToPeriod(
          OpenAlexMetricsToPeriodRequest request
  );

  // Topic
  TopicHotFilterResponse filterHotTopic(
          TopicHotFilterRequest request
  );

  Topic findTopicById(
          String topicId,
          String fieldId
  );

  long numOfWorksInPeriodByTopic(
          OpenAlexTopicFilterRequest request
  );

  OpenAlexWorkCitationResponse takeWorkCitationList(
          OpenAlexTopicFilterRequest request
  );

  long countInstitutionByTopic(
          OpenAlexTopicFilterRequest request
  );

  Set<String> takeDistinctAuthorIds(
          OpenAlexTopicFilterRequest request
  );

  // Keyword
  KeywordHotFilterResponse filterHotKeyword();

  long numOfWorksInPeriodByKeyword(
          String keywordId,
          LocalDate start,
          LocalDate end
  );

  long numOfWorksInPeriodByField(
          String fieldId,
          LocalDate start,
          LocalDate end
  );
}
