package com.brotherhood.scipubtts.detail.entities.service;

import com.brotherhood.scipubtts.detail.entities.dto.response.EntityDetailResponse;
import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.search.dto.response.SearchWorksResponse;
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
public class EntityDetailService {

    private static final int DETAIL_WORK_LIMIT = 8;
    private static final int GROUP_BY_LIMIT = 200;
    private static final int TYPE_BREAKDOWN_LIMIT = 8;

    private final OpenAlexClient openAlexClient;
    private final OpenAlexMapReader openAlexMapReader;
    private final SearchQuerySupport searchQuerySupport;
    private final SearchWorksMapper searchWorksMapper;

    public EntityDetailService(
            OpenAlexClient openAlexClient,
            OpenAlexMapReader openAlexMapReader,
            SearchQuerySupport searchQuerySupport,
            SearchWorksMapper searchWorksMapper
    ) {
        this.openAlexClient = openAlexClient;
        this.openAlexMapReader = openAlexMapReader;
        this.searchQuerySupport = searchQuerySupport;
        this.searchWorksMapper = searchWorksMapper;
    }

    public EntityDetailResponse getAuthorDetail(String rawAuthorId) {
        String authorId = normalizeEntityId(rawAuthorId);
        Map<String, Object> author = openAlexClient.get("/authors/" + authorId, Map.of());
        return buildAuthorDetailResponse(authorId, author);
    }

    public EntityDetailResponse getTopicDetail(String rawTopicId) {
        String topicId = normalizeEntityId(rawTopicId);
        Map<String, Object> topic = openAlexClient.get("/topics/" + topicId, Map.of());
        return buildTopicDetailResponse(topicId, topic);
    }

    private EntityDetailResponse buildAuthorDetailResponse(
            String authorId,
            Map<String, Object> author
    ) {
        List<Map<String, Object>> institutions =
                openAlexMapReader.getMapListFromObject(author.get("last_known_institutions"));
        List<Map<String, Object>> topics =
                openAlexMapReader.getMapListFromObject(author.get("topics"));
        Map<String, Object> summaryStats = openAlexMapReader.getMap(author, "summary_stats");

        return new EntityDetailResponse(
                "authors",
                authorId,
                normalizeText(openAlexMapReader.getString(author, "display_name")),
                null,
                normalizeText(openAlexMapReader.getString(author, "orcid")),
                readFirstDisplayName(institutions),
                null,
                null,
                null,
                openAlexMapReader.getLong(author, "works_count", 0L),
                openAlexMapReader.getLong(author, "cited_by_count", 0L),
                openAlexMapReader.getInteger(summaryStats, "h_index"),
                openAlexMapReader.getInteger(summaryStats, "i10_index"),
                mapObservedNames(author.get("display_name_alternatives")),
                mapInstitutionNames(institutions),
                mapRelatedItems(topics, "count"),
                List.of(),
                mapAuthorCountsByYear(author.get("counts_by_year")),
                List.of(),
                loadWorks("authorships.author.id:" + authorId)
        );
    }

    private EntityDetailResponse buildTopicDetailResponse(
            String topicId,
            Map<String, Object> topic
    ) {
        Map<String, Object> subfield = openAlexMapReader.getMap(topic, "subfield");
        Map<String, Object> field = openAlexMapReader.getMap(topic, "field");
        Map<String, Object> domain = openAlexMapReader.getMap(topic, "domain");

        return new EntityDetailResponse(
                "topics",
                topicId,
                normalizeText(openAlexMapReader.getString(topic, "display_name")),
                normalizeText(openAlexMapReader.getString(topic, "description")),
                null,
                null,
                normalizeText(openAlexMapReader.getString(subfield, "display_name")),
                normalizeText(openAlexMapReader.getString(field, "display_name")),
                normalizeText(openAlexMapReader.getString(domain, "display_name")),
                openAlexMapReader.getLong(topic, "works_count", 0L),
                openAlexMapReader.getLong(topic, "cited_by_count", 0L),
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                mapRelatedItems(topic.get("siblings"), null),
                loadTopicCountsByYear(topicId),
                loadTopicTypeBreakdown(topicId),
                loadWorks("topics.id:" + topicId)
        );
    }

    private List<SearchWorksResponse.WorkItem> loadWorks(String filterValue) {
        Map<String, String> queryParams = buildWorkQueryParams(filterValue);
        Map<String, Object> response = openAlexClient.get("/works", queryParams);
        List<Map<String, Object>> results = openAlexMapReader.getMapList(response, "results");
        return searchWorksMapper.mapWorkItems(results);
    }

    private Map<String, String> buildWorkQueryParams(String filterValue) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("filter", filterValue);
        queryParams.put("sort", "publication_date:desc");
        queryParams.put("per_page", String.valueOf(DETAIL_WORK_LIMIT));
        queryParams.put("select", SearchConstants.WORKS_SELECT_FIELDS);
        return queryParams;
    }

    private List<EntityDetailResponse.CountByYearItem> mapAuthorCountsByYear(Object rawCountsByYear) {
        List<Map<String, Object>> yearMaps = openAlexMapReader.getMapListFromObject(rawCountsByYear);
        List<EntityDetailResponse.CountByYearItem> items = new ArrayList<>();

        for (Map<String, Object> yearMap : yearMaps) {
            int year = openAlexMapReader.getInt(yearMap, "year", 0);

            if (year <= 0) {
                continue;
            }

            items.add(new EntityDetailResponse.CountByYearItem(
                    year,
                    openAlexMapReader.getLong(yearMap, "works_count", 0L),
                    openAlexMapReader.getLong(yearMap, "cited_by_count", 0L)
            ));
        }

        items.sort(Comparator.comparingInt(EntityDetailResponse.CountByYearItem::year));
        return items;
    }

    private List<EntityDetailResponse.CountByYearItem> loadTopicCountsByYear(String topicId) {
        Map<String, String> queryParams = buildTopicCountsByYearQueryParams(topicId);
        Map<String, Object> response = openAlexClient.get("/works", queryParams);
        List<Map<String, Object>> groups = openAlexMapReader.getMapList(response, "group_by");
        List<EntityDetailResponse.CountByYearItem> items = new ArrayList<>();

        for (Map<String, Object> group : groups) {
            int year = parsePositiveInt(openAlexMapReader.getString(group, "key"));

            if (year <= 0) {
                continue;
            }

            items.add(new EntityDetailResponse.CountByYearItem(
                    year,
                    openAlexMapReader.getLong(group, "count", 0L),
                    0L
            ));
        }

        items.sort(Comparator.comparingInt(EntityDetailResponse.CountByYearItem::year));
        return items;
    }

    private Map<String, String> buildTopicCountsByYearQueryParams(String topicId) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("filter", "topics.id:" + topicId);
        queryParams.put("group_by", "publication_year");
        queryParams.put("per_page", String.valueOf(GROUP_BY_LIMIT));
        return queryParams;
    }

    private List<EntityDetailResponse.BreakdownItem> loadTopicTypeBreakdown(String topicId) {
        Map<String, String> queryParams = buildTopicTypeBreakdownQueryParams(topicId);
        Map<String, Object> response = openAlexClient.get("/works", queryParams);
        List<Map<String, Object>> groups = openAlexMapReader.getMapList(response, "group_by");
        List<EntityDetailResponse.BreakdownItem> items = new ArrayList<>();

        for (Map<String, Object> group : groups) {
            String value = normalizeText(openAlexMapReader.getString(group, "key"));
            String label = normalizeText(openAlexMapReader.getString(group, "key_display_name"));

            if (!StringUtils.hasText(value) || !StringUtils.hasText(label)) {
                continue;
            }

            items.add(new EntityDetailResponse.BreakdownItem(
                    value,
                    label,
                    openAlexMapReader.getLong(group, "count", 0L)
            ));
        }

        return items;
    }

    private Map<String, String> buildTopicTypeBreakdownQueryParams(String topicId) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("filter", "topics.id:" + topicId);
        queryParams.put("group_by", "type");
        queryParams.put("sort", "count:desc");
        queryParams.put("per_page", String.valueOf(TYPE_BREAKDOWN_LIMIT));
        return queryParams;
    }

    private List<EntityDetailResponse.RelatedItem> mapRelatedItems(
            Object rawItems,
            String countKey
    ) {
        List<Map<String, Object>> items = openAlexMapReader.getMapListFromObject(rawItems);
        List<EntityDetailResponse.RelatedItem> relatedItems = new ArrayList<>();
        LinkedHashSet<String> seenIds = new LinkedHashSet<>();

        for (Map<String, Object> item : items) {
            String id = searchQuerySupport.normalizeEntityValue(
                    openAlexMapReader.getString(item, "id")
            );
            String displayName = normalizeText(openAlexMapReader.getString(item, "display_name"));

            if (!StringUtils.hasText(id) || !StringUtils.hasText(displayName) || seenIds.contains(id)) {
                continue;
            }

            Long count = null;

            if (StringUtils.hasText(countKey)) {
                count = openAlexMapReader.getLong(item, countKey, 0L);
            }

            relatedItems.add(new EntityDetailResponse.RelatedItem(id, displayName, count));
            seenIds.add(id);

            if (relatedItems.size() == TYPE_BREAKDOWN_LIMIT) {
                break;
            }
        }

        return relatedItems;
    }

    private List<String> mapObservedNames(Object rawObservedNames) {
        LinkedHashSet<String> observedNames = new LinkedHashSet<>();

        for (Object rawObservedName : openAlexMapReader.getObjectList(rawObservedNames)) {
            String observedName = normalizeText(String.valueOf(rawObservedName));

            if (StringUtils.hasText(observedName)) {
                observedNames.add(observedName);
            }
        }

        return new ArrayList<>(observedNames);
    }

    private List<String> mapInstitutionNames(List<Map<String, Object>> institutions) {
        LinkedHashSet<String> names = new LinkedHashSet<>();

        for (Map<String, Object> institution : institutions) {
            String institutionName = normalizeText(
                    openAlexMapReader.getString(institution, "display_name")
            );

            if (StringUtils.hasText(institutionName)) {
                names.add(institutionName);
            }
        }

        return new ArrayList<>(names);
    }

    private String readFirstDisplayName(List<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            String displayName = normalizeText(
                    openAlexMapReader.getString(item, "display_name")
            );

            if (StringUtils.hasText(displayName)) {
                return displayName;
            }
        }

        return null;
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
