package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.dashboard.dto.request.*;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexMetricsInPeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexMetricsToPeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexPublicationRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexTopicFilterRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.*;
import com.brotherhood.scipubtts.dashboard.dto.response.openalex.*;
import com.brotherhood.scipubtts.dashboard.entity.Topic;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.*;

@Component
public class OpenAlexService {
  private final RestClient restClient;
  private final int TOP_TOPICS = 30;

  public OpenAlexService(RestClient openAlexClient) {
    this.restClient = openAlexClient;
  }

  // Service for publication
  public OpenAlexPublicationResponse searchPublicationsByYear(OpenAlexPublicationRequest request){
    if(request.yearFrom().isEmpty() || request.yearTo().isEmpty()) {
      return null;
    }

    var queryPeriod = String.format("publication_year:%s-%s", request.yearFrom(), request.yearTo());

    return restClient.get()
            .uri(uriBuilder ->
                    uriBuilder
                            .path("/works")
                            .queryParam(
                                    "filter",
                                    queryPeriod
                            )
                            .queryParam(
                                    "group_by",
                                    "publication_year"
                            )
                            .build())
            .retrieve()
            .body(OpenAlexPublicationResponse.class);
  }

  // Service for metric
  public OpenAlexMetricsResponse takeMetricsInPeriod(OpenAlexMetricsInPeriodRequest request){
    if(request.endTime().isEmpty() || request.startTime().isEmpty()){
      return null;
    }

    var queryPeriod = String.format("from_publication_date:%s,to_publication_date:%s",
            request.startTime(), request.endTime());

    return restClient.get()
            .uri(uriBuilder ->
                    uriBuilder
                            .path("/works")
                            .queryParam(
                                    "filter",
                                    queryPeriod
                            )
                            .queryParam(
                                    "page",
                                    1
                            ).queryParam(
                                    "per_page",
                                    1
                            ).queryParam(
                                    "select",
                                  "id"
                            )
                            .build())
            .retrieve()
            .body(OpenAlexMetricsResponse.class);
  }

  public OpenAlexMetricsResponse takeMetricsToPeriod(
          OpenAlexMetricsToPeriodRequest request) {
    var path = String.format(("/%s"), request.entity().getPath());

    return restClient.get()
            .uri(uriBuilder ->
                    uriBuilder
                            .path(path)
                            .queryParam(
                                    "page",
                                    1
                            ).queryParam(
                                    "per_page",
                                    1
                            ).queryParam(
                                    "select",
                                    "id"
                            )
                            .build())
            .retrieve()
            .body(OpenAlexMetricsResponse.class);
  }

  // Service for topic
  private Topic toTopic(
          OpenAlexHotTopicFilterResponse.TopicItem item,
          Integer fieldId
  ) {
    Topic topic = new Topic();

    topic.setTopicId(item.id());
    topic.setName(item.displayName());
    topic.setFieldId(fieldId);
    topic.setWorks(
            item.worksCount() == null
                    ? 0
                    : item.worksCount()
    );
    topic.setCitations(
            item.citedByCount() == null
                    ? 0
                    : item.citedByCount()
    );

    return topic;
  }

  public TopicHotFilterResponse filterHotTopic(
          TopicHotFilterRequest request
  ){
    var queryPeriod = String.format("field.id:%s", request.fieldId());
    Integer fieldId = Integer.parseInt(request.fieldId());

    var topOfCited = restClient.get()
            .uri(uriBuilder ->
                    uriBuilder
                            .path("/topics")
                            .queryParam(
                                    "filter",
                                    queryPeriod
                            )
                            .queryParam(
                                    "page",
                                    1
                            ).queryParam(
                                    "per_page",
                                    TOP_TOPICS
                            ).queryParam(
                                    "select",
                                    "id,display_name,works_count,cited_by_count"
                            ).queryParam(
                                    "sort",
                                  "cited_by_count:desc"
                            )
                            .build())
            .retrieve()
            .body(OpenAlexHotTopicFilterResponse.class);

    var topOfWorks = restClient.get()
            .uri(uriBuilder ->
                    uriBuilder
                            .path("/topics")
                            .queryParam(
                                    "filter",
                                    queryPeriod
                            )
                            .queryParam(
                                    "page",
                                    1
                            ).queryParam(
                                    "per_page",
                                    TOP_TOPICS
                            ).queryParam(
                                    "select",
                                    "id,display_name,works_count,cited_by_count"
                            ).queryParam(
                                    "sort",
                                    "works_count:desc"
                            )
                            .build())
            .retrieve()
            .body(OpenAlexHotTopicFilterResponse.class);

    Set<String> visited =
            new LinkedHashSet<>();

    List<Topic> topics =
            new ArrayList<>();

    if (topOfCited != null) {

      topOfCited.results()
              .forEach(item -> {

                if (visited.add(item.id())) {
                  topics.add(toTopic(item, fieldId));
                }

              });

    }

    if (topOfWorks != null) {

      topOfWorks.results()
              .forEach(item -> {

                if (visited.add(item.id())) {
                  topics.add(toTopic(item, fieldId));
                }

              });

    }

    return new TopicHotFilterResponse(
            List.copyOf(
                    topics
            )
    );
  }

  public Topic findTopicById(String topicId, String fieldId) {

    var response = restClient.get()
            .uri(uriBuilder ->
                    uriBuilder
                            .path("/topics")
                            .queryParam(
                                    "filter",
                                    "id:" + topicId
                            )
                            .queryParam(
                                    "per_page",
                                    1
                            )
                            .queryParam(
                                    "select",
                                    "id,display_name,works_count,cited_by_count"
                            )
                            .build()
            )
            .retrieve()
            .body(OpenAlexHotTopicFilterResponse.class);

    if (response == null
            || response.results() == null
            || response.results().isEmpty()) {

      throw new BusinessException(
              ErrorCode.TOPIC_NOT_FOUND
      );
    }

    return toTopic(response.results().getFirst(),
            Integer.parseInt(fieldId)
    );
  }

  public long numOfWorksInPeriodByTopic(
          OpenAlexTopicFilterRequest request
  ){
    var filter = String.format(
            "from_publication_date:%s,to_publication_date:%s,topics.id:%s",
            request.startTime(),
            request.endTime(),
            request.topicId()
    );

    var response = restClient.get()
            .uri(uriBuilder ->
                    uriBuilder
                            .path("/works")
                            .queryParam(
                                    "filter",
                                    filter
                            )
                            .queryParam(
                                    "page",
                                    1
                            )
                            .queryParam(
                                    "per-page",
                                    1
                            )
                            .queryParam(
                                    "select",
                                    "id"
                            )
                            .build()
            )
            .retrieve()
            .body(OpenAlexMetricsResponse.class);

    if (response == null || response.meta() == null) {
      throw new BusinessException(
              ErrorCode.OPENALEX_SERVICE_ERROR
      );
    }

    return response.meta().count();
  }

  public OpenAlexWorkCitationResponse takeWorkCitationList(
          OpenAlexTopicFilterRequest request
  ){
    int startYear = LocalDate.parse(request.startTime()).getYear();
    int endYear = LocalDate.parse(request.endTime()).getYear();

    List<OpenAlexWorkCitationResponse.WorkCitation> result =
            new ArrayList<>();

    int apiCallCount = 0;
    for (int publicationYear = startYear;
         publicationYear <= endYear;
         publicationYear++){

      int diff = endYear - publicationYear;

      // 2^diff - 1
      int citationThreshold =(1 << diff) - 1;

      String filter =
              String.format(
                      "topics.id:%s," +
                              "from_publication_date:%s," +
                              "to_publication_date:%s," +
                              "publication_year:%d," +
                              "cited_by_count:>%d",
                      request.topicId(),
                      request.startTime(),
                      request.endTime(),
                      publicationYear,
                      citationThreshold
              );

      int page = 1;

      while (true) {
        var currentPage = page;

        apiCallCount++;
        var response =
                restClient.get()
                        .uri(uriBuilder ->
                                uriBuilder
                                        .path("/works")
                                        .queryParam("filter", filter)
                                        .queryParam(
                                                "select",
                                                "cited_by_count,publication_date"
                                        )
                                        .queryParam(
                                                "sort",
                                                "cited_by_count:desc"
                                        )
                                        .queryParam("per_page", 200)
                                        .queryParam("page", currentPage)
                                        .build()
                        )
                        .retrieve()
                        .body(OpenAlexWorkCitationResponse.class);

        if (response == null
                || response.workCitationList() == null
                || response.workCitationList().isEmpty()) {
          break;
        }

        result.addAll(response.workCitationList());

        long totalCount = response.meta().count();

        if (page * 200 >= totalCount) {
          break;
        }

        page++;
      }
    }

    System.out.println("Total API calls made: " + apiCallCount);
    return new OpenAlexWorkCitationResponse(null, result);
  }

  public long countInstitutionByTopic(
          OpenAlexTopicFilterRequest request
  ) {

    String filter =
            String.format(
                    "topics.id:%s," +
                            "from_publication_date:%s," +
                            "to_publication_date:%s",
                    request.topicId(),
                    request.startTime(),
                    request.endTime()
            );

    long total = 0;
    String cursor = "*";

    while (cursor != null) {
      final String currentCursor = cursor;

      OpenAlexInstitutionResponse response = restClient.get()
              .uri(uriBuilder -> uriBuilder
                      .path("/works")
                      .queryParam("filter", filter)
                      .queryParam("group_by", "authorships.institutions.id")
                      .queryParam("per_page", 200)
                      .queryParam("cursor", currentCursor)
                      .build()
              )
              .retrieve()
              .body(OpenAlexInstitutionResponse.class);

      if (response == null || response.groupBy() == null) break;

      int pageCount = response.groupBy().size();
      total += pageCount;

      if (pageCount < 200) break; // page cuối

      cursor = response.meta().nextCursor(); // null nếu hết
    }

    return total;
  }

  public Set<String> takeDistinctAuthorIds(
          OpenAlexTopicFilterRequest request
  ) {

    String filter = String.format(
            "topics.id:%s,from_publication_date:%s,to_publication_date:%s",
            request.topicId(),
            request.startTime(),
            request.endTime()
    );

    Set<String> authorIds = new HashSet<>();
    String cursor = "*";

    while (cursor != null) {
      var currentCursor = cursor;

      OpenAlexAuthorGroupResponse response = restClient.get()
              .uri(uriBuilder -> uriBuilder
                      .path("/works")
                      .queryParam("filter", filter)
                      .queryParam("group_by", "author.id")
                      .queryParam("per_page", 200)
                      .queryParam("cursor", currentCursor)
                      .build()
              )
              .retrieve()
              .body(OpenAlexAuthorGroupResponse.class);

      if (response == null || response.groupBy() == null) break;

      response.groupBy().stream()
              .map(OpenAlexAuthorGroupResponse.Group::key)
              .filter(key -> key != null && !key.isBlank())
              .forEach(authorIds::add);

      int pageSize = response.groupBy().size();
      if (pageSize < 200) break;

      cursor = response.meta() != null ? response.meta().nextCursor() : null;
    }

    return authorIds;
  }
}