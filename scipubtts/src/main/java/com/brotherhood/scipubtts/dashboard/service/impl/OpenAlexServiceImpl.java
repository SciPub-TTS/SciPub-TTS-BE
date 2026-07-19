package com.brotherhood.scipubtts.dashboard.service.impl;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.common.openalex.OpenAlexCursorSupport;
import com.brotherhood.scipubtts.dashboard.constant.OpenAlexEntity;
import com.brotherhood.scipubtts.dashboard.dto.request.*;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexMetricsInPeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexMetricsToPeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexPublicationRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexTopicFilterRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.*;
import com.brotherhood.scipubtts.dashboard.dto.response.openalex.*;
import com.brotherhood.scipubtts.dashboard.entity.Keyword;
import com.brotherhood.scipubtts.dashboard.entity.Topic;
import com.brotherhood.scipubtts.dashboard.service.OpenAlexService;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OpenAlexServiceImpl implements OpenAlexService {
  private final RestClient restClient;
  private final int TOP_TOPICS = 30;
  private final int TOP_KEYWORDS = 100;
  private static final int MAX_CITATION_PAPERS = 1000;
  private static final int CITATION_THRESHOLD_BOOST = 2;

  public OpenAlexServiceImpl(RestClient openAlexClient) {
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
    if(request.endTime() == null || request.startTime() == null
            || request.endTime().isEmpty() || request.startTime().isEmpty()){
      return null;
    }

    List<String> filters = new ArrayList<>();
    filters.add("from_publication_date:" + request.startTime());
    filters.add("to_publication_date:" + request.endTime());

    if (request.fieldIds() != null && !request.fieldIds().isEmpty()) {
      String fieldValues = request.fieldIds().stream()
              .map(String::valueOf)
              .collect(Collectors.joining("|"));
      filters.add("primary_topic.field.id:" + fieldValues);
    }

    var queryPeriod = String.join(",", filters);

    return restClient.get()
            .uri(uriBuilder ->
                    uriBuilder
                            .path("/works")
                            .queryParam(
                                    "filter",
                                    queryPeriod
                            )
                            .queryParam(
                                    "cursor",
                                    OpenAlexCursorSupport.INITIAL_CURSOR
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
            .uri(uriBuilder -> {
              var builder = uriBuilder
                      .path(path)
                      .queryParam("cursor", OpenAlexCursorSupport.INITIAL_CURSOR)
                      .queryParam("per_page", 1)
                      .queryParam("select", "id");

              List<String> filters = new ArrayList<>();

              boolean canFilterByField = request.fieldIds() != null
                      && !request.fieldIds().isEmpty()
                      && request.entity() != OpenAlexEntity.KEYWORDS;

              if (canFilterByField) {
                String filterKey = request.entity() == OpenAlexEntity.TOPICS
                        ? "field.id"
                        : "primary_topic.field.id";

                String fieldValues = request.fieldIds().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining("|"));

                filters.add(filterKey + ":" + fieldValues);
              }

              boolean canFilterByDate = request.entity() == OpenAlexEntity.WORKS
                      && request.endTime() != null;

              if (canFilterByDate) {
                filters.add("to_publication_date:" + request.endTime());
              }

              if (!filters.isEmpty()) {
                builder.queryParam("filter", String.join(",", filters));
              }

              return builder.build();
            })
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
                                    "cursor",
                                    OpenAlexCursorSupport.INITIAL_CURSOR
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
                                    "cursor",
                                    OpenAlexCursorSupport.INITIAL_CURSOR
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
                                    "cursor",
                                    OpenAlexCursorSupport.INITIAL_CURSOR
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
                                    "cursor",
                                    OpenAlexCursorSupport.INITIAL_CURSOR
                            )
                            .queryParam(
                                    "per_page",
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

    outer:
    for (int publicationYear = endYear;
         publicationYear >= startYear;
         publicationYear--){

      int diff = endYear - publicationYear;

      int citationThreshold = (1 << (diff + CITATION_THRESHOLD_BOOST)) - 1;

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

      String cursor = OpenAlexCursorSupport.INITIAL_CURSOR;

      while (cursor != null) {
        final String currentCursor = cursor;

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
                                        .queryParam(
                                                "per_page",
                                                OpenAlexCursorSupport.MAX_PER_PAGE
                                        )
                                        .queryParam("cursor", currentCursor)
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

        if (result.size() >= MAX_CITATION_PAPERS) {
          break outer;
        }

        cursor = response.meta() != null
                && OpenAlexCursorSupport.hasNextCursor(response.meta().nextCursor())
                ? response.meta().nextCursor()
                : null;
      }
    }

    System.out.println("Total API calls made: " + apiCallCount);

    List<OpenAlexWorkCitationResponse.WorkCitation> capped =
            result.size() > MAX_CITATION_PAPERS
                    ? result.subList(0, MAX_CITATION_PAPERS)
                    : result;

    return new OpenAlexWorkCitationResponse(null, capped);
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
    String cursor = OpenAlexCursorSupport.INITIAL_CURSOR;

    while (cursor != null) {
      final String currentCursor = cursor;

      OpenAlexInstitutionResponse response = restClient.get()
              .uri(uriBuilder -> uriBuilder
                      .path("/works")
                      .queryParam("filter", filter)
                      .queryParam("group_by", "authorships.institutions.id")
                      .queryParam("per_page", OpenAlexCursorSupport.MAX_PER_PAGE)
                      .queryParam("cursor", currentCursor)
                      .build()
              )
              .retrieve()
              .body(OpenAlexInstitutionResponse.class);

      if (response == null || response.groupBy() == null) break;

      total += response.groupBy().size();

      cursor = response.meta() != null
              && OpenAlexCursorSupport.hasNextCursor(response.meta().nextCursor())
              ? response.meta().nextCursor()
              : null;
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
    String cursor = OpenAlexCursorSupport.INITIAL_CURSOR;

    while (cursor != null) {
      var currentCursor = cursor;

      OpenAlexAuthorGroupResponse response = restClient.get()
              .uri(uriBuilder -> uriBuilder
                      .path("/works")
                      .queryParam("filter", filter)
                      .queryParam("group_by", "author.id")
                      .queryParam("per_page", OpenAlexCursorSupport.MAX_PER_PAGE)
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

      cursor = response.meta() != null
              && OpenAlexCursorSupport.hasNextCursor(response.meta().nextCursor())
              ? response.meta().nextCursor()
              : null;
    }

    return authorIds;
  }

  // Service for keyword

  private Keyword toKeyword(OpenAlexHotKeywordFilterResponse.KeywordItem item) {
    Keyword keyword = new Keyword();

    keyword.setKeywordId(item.id());
    keyword.setKeyword(item.displayName());
    keyword.setWorksCount(
            item.worksCount() == null
                    ? 0
                    : item.worksCount()
    );
    keyword.setCitedByCount(
            item.citedByCount() == null
                    ? 0
                    : item.citedByCount()
    );

    return keyword;
  }

  public KeywordHotFilterResponse filterHotKeyword() {

    var topOfCited = restClient.get()
            .uri(uriBuilder ->
                    uriBuilder
                            .path("/keywords")
                            .queryParam("cursor", OpenAlexCursorSupport.INITIAL_CURSOR)
                            .queryParam("per_page", TOP_KEYWORDS)
                            .queryParam("select", "id,display_name,works_count,cited_by_count")
                            .queryParam("sort", "cited_by_count:desc")
                            .build())
            .retrieve()
            .body(OpenAlexHotKeywordFilterResponse.class);

    var topOfWorks = restClient.get()
            .uri(uriBuilder ->
                    uriBuilder
                            .path("/keywords")
                            .queryParam("cursor", OpenAlexCursorSupport.INITIAL_CURSOR)
                            .queryParam("per_page", TOP_KEYWORDS)
                            .queryParam("select", "id,display_name,works_count,cited_by_count")
                            .queryParam("sort", "works_count:desc")
                            .build())
            .retrieve()
            .body(OpenAlexHotKeywordFilterResponse.class);

    Set<String> visited = new LinkedHashSet<>();
    List<Keyword> keywords = new ArrayList<>();

    if (topOfCited != null) {
      topOfCited.keywordItemList()
              .forEach(item -> {
                if (visited.add(item.id())) {
                  keywords.add(toKeyword(item));
                }
              });
    }

    if (topOfWorks != null) {
      topOfWorks.keywordItemList()
              .forEach(item -> {
                if (visited.add(item.id())) {
                  keywords.add(toKeyword(item));
                }
              });
    }

    return new KeywordHotFilterResponse(List.copyOf(keywords));
  }

  public long numOfWorksInPeriodByKeyword(
          String keywordId,
          LocalDate start,
          LocalDate end
  ) {
    String filter = String.format(
            "keywords.id:%s,from_publication_date:%s,to_publication_date:%s,type:article",
            keywordId, start, end
    );

    long total = 0L;
    String cursor = OpenAlexCursorSupport.INITIAL_CURSOR;

    while (cursor != null) {
      final String currentCursor = cursor;

      var response = restClient.get()
              .uri(uriBuilder -> uriBuilder
                      .path("/works")
                      .queryParam("filter", filter)
                      .queryParam("group_by", "publication_year")
                      .queryParam("per_page", OpenAlexCursorSupport.MAX_PER_PAGE)
                      .queryParam("cursor", currentCursor)
                      .build()
              )
              .retrieve()
              .body(OpenAlexGroupByResponse.class);

      if (response == null || response.groupBy() == null) break;

      total += response.groupBy().stream()
              .mapToLong(OpenAlexGroupByResponse.GroupByItem::count)
              .sum();

      cursor = response.meta() != null
              && OpenAlexCursorSupport.hasNextCursor(response.meta().nextCursor())
              ? response.meta().nextCursor()
              : null;
    }

    return total;
  }

  public long numOfWorksInPeriodByField(
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
                    .queryParam("cursor", OpenAlexCursorSupport.INITIAL_CURSOR)
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