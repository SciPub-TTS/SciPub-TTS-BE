package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.dashboard.dto.request.TopicHotFilterRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexMetricsInPeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexMetricsToPeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexPublicationRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexTopicFilterRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.KeywordHotFilterResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.TopicHotFilterResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.openalex.OpenAlexAuthorGroupResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.openalex.OpenAlexGroupByResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.openalex.OpenAlexHotKeywordFilterResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.openalex.OpenAlexHotTopicFilterResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.openalex.OpenAlexInstitutionResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.openalex.OpenAlexMetricsResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.openalex.OpenAlexPublicationResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.openalex.OpenAlexWorkCitationResponse;
import com.brotherhood.scipubtts.dashboard.entity.Keyword;
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
  ) {
    String filter = String.format(
            "primary_topic.field.id:%s,from_publication_date:%s,to_publication_date:%s,type:article",
            fieldId, start, end
    );

    var response = restClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/works")
                    .queryParam("filter", filter)
                    .queryParam("page", 1)
                    .queryParam("per_page", 1)
                    .queryParam("select", "id")
                    .build()
            )
            .retrieve()
            .body(OpenAlexMetricsResponse.class);

    if (response == null || response.meta() == null) return 0L;

    return response.meta().count();
  }
}
