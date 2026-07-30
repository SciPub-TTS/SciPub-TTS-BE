package com.brotherhood.scipubtts.detail.topics.service.impl;

import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.detail.topics.dto.response.TopicDetailResponse;
import com.brotherhood.scipubtts.detail.topics.service.TopicDetailService;
import com.brotherhood.scipubtts.detail.works.dto.response.DetailWorkResponse;
import com.brotherhood.scipubtts.detail.works.service.DetailWorkResponseMapper;
import com.brotherhood.scipubtts.search.service.OpenAlexMapReader;
import com.brotherhood.scipubtts.search.service.SearchConstants;
import com.brotherhood.scipubtts.search.service.SearchQuerySupport;
import com.brotherhood.scipubtts.search.service.SearchWorksMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class TopicDetailServiceImpl implements TopicDetailService {

    private static final int DETAIL_WORK_LIMIT = 8;
    private static final int GROUP_BY_LIMIT = 200;
    private static final int RELATED_TOPIC_LIMIT = 8;
    private static final int TYPE_BREAKDOWN_LIMIT = 8;

    private final DetailWorkResponseMapper detailWorkResponseMapper;
    private final OpenAlexClient openAlexClient;
    private final OpenAlexMapReader openAlexMapReader;
    private final SearchQuerySupport searchQuerySupport;
    private final SearchWorksMapper searchWorksMapper;

    public TopicDetailServiceImpl(
            DetailWorkResponseMapper detailWorkResponseMapper,
            OpenAlexClient openAlexClient,
            OpenAlexMapReader openAlexMapReader,
            SearchQuerySupport searchQuerySupport,
            SearchWorksMapper searchWorksMapper
    ) {
        this.detailWorkResponseMapper = detailWorkResponseMapper;
        this.openAlexClient = openAlexClient;
        this.openAlexMapReader = openAlexMapReader;
        this.searchQuerySupport = searchQuerySupport;
        this.searchWorksMapper = searchWorksMapper;
    }

    @Override
    public TopicDetailResponse getTopicDetail(String rawTopicId) {
        String topicId = normalizeEntityId(rawTopicId);
        Map<String, Object> topic = openAlexClient.get("/topics/" + topicId, Map.of());

        return buildTopicDetailResponse(topicId, topic);
    }

    private TopicDetailResponse buildTopicDetailResponse(
            String topicId,
            Map<String, Object> topic
    ) {
        Map<String, Object> subfield = openAlexMapReader.getMap(topic, "subfield");
        Map<String, Object> field = openAlexMapReader.getMap(topic, "field");
        Map<String, Object> domain = openAlexMapReader.getMap(topic, "domain");

        return new TopicDetailResponse(
                "topics",
                topicId,
                normalizeText(openAlexMapReader.getString(topic, "display_name")),
                openAlexMapReader.getLong(topic, "works_count", 0L),
                openAlexMapReader.getLong(topic, "cited_by_count", 0L),
                loadWorks("topics.id:" + topicId),
                loadCountsByYear(topicId),
                normalizeText(openAlexMapReader.getString(topic, "description")),
                normalizeText(openAlexMapReader.getString(domain, "display_name")),
                normalizeText(openAlexMapReader.getString(field, "display_name")),
                mapSiblingTopics(topic.get("siblings")),
                normalizeText(openAlexMapReader.getString(subfield, "display_name")),
                loadTypeBreakdown(topicId)
        );
    }

    private List<DetailWorkResponse> loadWorks(String filterValue) {
        Map<String, String> queryParams = new LinkedHashMap<>();

        queryParams.put("filter", filterValue);
        queryParams.put("sort", "publication_date:desc");
        queryParams.put("per_page", String.valueOf(DETAIL_WORK_LIMIT));
        queryParams.put("select", SearchConstants.WORKS_SELECT_FIELDS);

        Map<String, Object> response = openAlexClient.get("/works", queryParams);
        List<Map<String, Object>> results = openAlexMapReader.getMapList(response, "results");

        return detailWorkResponseMapper.mapWorkItems(searchWorksMapper.mapWorkItems(results));
    }

    private List<TopicDetailResponse.YearStat> loadCountsByYear(String topicId) {
        Map<String, String> queryParams = buildCountsByYearQueryParams(topicId);
        Map<String, Object> response = openAlexClient.get("/works", queryParams);
        List<Map<String, Object>> groups = openAlexMapReader.getMapList(response, "group_by");
        List<TopicDetailResponse.YearStat> items = new ArrayList<>();

        for (Map<String, Object> group : groups) {
            int year = parsePositiveInt(openAlexMapReader.getString(group, "key"));

            if (year <= 0) {
                continue;
            }

            items.add(new TopicDetailResponse.YearStat(
                    year,
                    openAlexMapReader.getLong(group, "count", 0L),
                    0L
            ));
        }

        items.sort(Comparator.comparingInt(TopicDetailResponse.YearStat::year));
        return items;
    }

    private Map<String, String> buildCountsByYearQueryParams(String topicId) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("filter", "topics.id:" + topicId);
        queryParams.put("group_by", "publication_year");
        queryParams.put("per_page", String.valueOf(GROUP_BY_LIMIT));

        return queryParams;
    }

    private List<TopicDetailResponse.TypeBreakdownItem> loadTypeBreakdown(String topicId) {
        Map<String, String> queryParams = buildTypeBreakdownQueryParams(topicId);
        Map<String, Object> response = openAlexClient.get("/works", queryParams);
        List<Map<String, Object>> groups = openAlexMapReader.getMapList(response, "group_by");
        List<TopicDetailResponse.TypeBreakdownItem> items = new ArrayList<>();

        for (Map<String, Object> group : groups) {
            String value = normalizeText(openAlexMapReader.getString(group, "key"));
            String label = normalizeText(openAlexMapReader.getString(group, "key_display_name"));

            if (!StringUtils.hasText(value) || !StringUtils.hasText(label)) {
                continue;
            }

            items.add(new TopicDetailResponse.TypeBreakdownItem(
                    value,
                    label,
                    openAlexMapReader.getLong(group, "count", 0L)
            ));
        }

        return items;
    }

    private Map<String, String> buildTypeBreakdownQueryParams(String topicId) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("filter", "topics.id:" + topicId);
        queryParams.put("group_by", "type");
        queryParams.put("sort", "count:desc");
        queryParams.put("per_page", String.valueOf(TYPE_BREAKDOWN_LIMIT));

        return queryParams;
    }

    private List<TopicDetailResponse.RelatedItem> mapSiblingTopics(Object rawSiblings) {
        List<Map<String, Object>> siblings = openAlexMapReader.getMapListFromObject(rawSiblings);
        List<TopicDetailResponse.RelatedItem> relatedItems = new ArrayList<>();
        LinkedHashSet<String> seenIds = new LinkedHashSet<>();

        for (Map<String, Object> sibling : siblings) {
            String id = searchQuerySupport.normalizeEntityValue(
                    openAlexMapReader.getString(sibling, "id")
            );
            String displayName = normalizeText(openAlexMapReader.getString(sibling, "display_name"));

            if (!StringUtils.hasText(id) || !StringUtils.hasText(displayName) || seenIds.contains(id)) {
                continue;
            }

            relatedItems.add(new TopicDetailResponse.RelatedItem(id, displayName, null));
            seenIds.add(id);

            if (relatedItems.size() == RELATED_TOPIC_LIMIT) {
                break;
            }
        }

        return relatedItems;
    }

    private String normalizeEntityId(String rawEntityId) {
        String normalizedId = searchQuerySupport.extractLastSegment(
                rawEntityId == null ? "" : rawEntityId.trim()
        ).toUpperCase(Locale.ROOT);

        if (!StringUtils.hasText(normalizedId)) {
            return "";
        }

        return normalizedId;
    }

    private String normalizeText(String value) {
        String normalizedValue = openAlexMapReader.sanitizeDisplayText(value);

        if (!StringUtils.hasText(normalizedValue)) {
            return null;
        }

        return normalizedValue;
    }

    private int parsePositiveInt(String value) {
        if (!StringUtils.hasText(value)) {
            return 0;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
