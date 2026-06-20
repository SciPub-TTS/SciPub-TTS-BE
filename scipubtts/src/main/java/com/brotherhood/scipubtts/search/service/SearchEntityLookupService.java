package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.search.dto.response.SearchEntitiesResponse;
import com.brotherhood.scipubtts.search.dto.request.SearchEntityQueryRequest;
import com.brotherhood.scipubtts.search.dto.SearchEntityType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SearchEntityLookupService {

    private final OpenAlexClient openAlexClient;
    private final SearchQuerySupport searchQuerySupport;
    private final OpenAlexMapReader openAlexMapReader;
    private final SearchScopeSupport searchScopeSupport;

    public SearchEntityLookupService(
            OpenAlexClient openAlexClient,
            SearchQuerySupport searchQuerySupport,
            OpenAlexMapReader openAlexMapReader,
            SearchScopeSupport searchScopeSupport
    ) {
        this.openAlexClient = openAlexClient;
        this.searchQuerySupport = searchQuerySupport;
        this.openAlexMapReader = openAlexMapReader;
        this.searchScopeSupport = searchScopeSupport;
    }

    public SearchEntitiesResponse searchEntities(
            SearchEntityType entityType,
            SearchEntityQueryRequest request
    ) {
        SearchEntityType safeEntityType =
                entityType == null ? SearchEntityType.WORKS : entityType;
        int page = searchQuerySupport.normalizeWorksPage(
                request == null ? null : request.page()
        );
        int perPage = searchQuerySupport.normalizePerPage(
                request == null ? null : request.perPage()
        );
        String query = searchQuerySupport.normalizeKeyword(
                request == null ? null : request.query()
        );
        List<String> institutionIds = searchQuerySupport.normalizeEntityIds(
                request == null ? null : request.institution()
        );
        List<String> countryCodes = searchQuerySupport.normalizeCountryValues(
                request == null ? null : request.country()
        );
        List<String> primaryTopicIds = searchQuerySupport.normalizeEntityIds(
                request == null ? null : request.primaryTopic()
        );
        List<String> subFieldIds = searchQuerySupport.normalizeSubFieldValues(
                request == null ? null : request.subField()
        );
        List<String> fieldIds = searchQuerySupport.normalizeEntityIds(
                request == null ? null : request.field()
        );
        boolean hasSearchQuery = StringUtils.hasText(query);
        String resolvedSort = searchQuerySupport.resolveEntitySort(
                request == null ? null : request.sortBy(),
                request == null ? null : request.sortDirection(),
                hasSearchQuery
        );

        if (
                SearchEntityType.WORKS.equals(safeEntityType)
                        || !hasEntitySearchCriteria(
                        safeEntityType,
                        hasSearchQuery,
                        institutionIds,
                        countryCodes,
                        primaryTopicIds,
                        subFieldIds,
                        fieldIds
                )
        ) {
            return emptyResponse(safeEntityType, page, perPage);
        }

        if (searchScopeSupport.requiresTopicProfileScope(safeEntityType)) {
            return searchEntitiesWithinScopedTopicProfile(
                    safeEntityType,
                    query,
                    institutionIds,
                    countryCodes,
                    primaryTopicIds,
                    page,
                    perPage,
                    resolvedSort
            );
        }

        return searchEntitiesWithDirectFilter(
                safeEntityType,
                query,
                subFieldIds,
                fieldIds,
                page,
                perPage,
                resolvedSort
        );
    }

    private SearchEntitiesResponse searchEntitiesWithDirectFilter(
            SearchEntityType entityType,
            String query,
            List<String> subFieldIds,
            List<String> fieldIds,
            int page,
            int perPage,
            String resolvedSort
    ) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("page", String.valueOf(page));
        queryParams.put("per_page", String.valueOf(perPage));
        queryParams.put("select", entityType.selectFields());
        String directFilter = searchScopeSupport.getDirectFilter(entityType);
        String nameOnlyFilter = buildEntityNameFilter(query);
        String appliedFilter = combineFilters(
                nameOnlyFilter,
                directFilter,
                buildMultiValueFilter("subfield.id", subFieldIds),
                buildMultiValueFilter("field.id", fieldIds)
        );

        if (StringUtils.hasText(appliedFilter)) {
            queryParams.put("filter", appliedFilter);
        }

        queryParams.put("sort", resolvedSort);

        Map<String, Object> openAlexResponse = openAlexClient.get(
                entityType.path(),
                queryParams
        );

        return mapDirectResponse(entityType, openAlexResponse, page, perPage);
    }

    private SearchEntitiesResponse searchEntitiesWithinScopedTopicProfile(
            SearchEntityType entityType,
            String query,
            List<String> institutionIds,
            List<String> countryCodes,
            List<String> primaryTopicIds,
            int page,
            int perPage,
            String resolvedSort
    ) {
        int fromIndex = Math.max((page - 1) * perPage, 0);
        int pageEndExclusive = fromIndex + perPage;
        int requiredScopedResultCount = pageEndExclusive + 1;
        int openAlexPage = 1;
        boolean reachedEnd = false;
        long totalDbResponseTimeMs = 0L;
        double totalCostUsd = 0D;
        Set<String> seenIds = new LinkedHashSet<>();
        List<Map<String, Object>> scopedRawResults = new ArrayList<>();

        while (!reachedEnd && scopedRawResults.size() < requiredScopedResultCount) {
            Map<String, String> queryParams = new LinkedHashMap<>();
            queryParams.put("page", String.valueOf(openAlexPage));
            queryParams.put(
                    "per_page",
                    String.valueOf(SearchConstants.ENTITY_SCOPE_FETCH_PER_PAGE)
            );
            queryParams.put("select", entityType.selectFields());
            String nameOnlyFilter = buildEntityNameFilter(query);

            if (StringUtils.hasText(nameOnlyFilter)) {
                queryParams.put("filter", nameOnlyFilter);
            }

            queryParams.put("sort", resolvedSort);

            Map<String, Object> openAlexResponse = openAlexClient.get(
                    entityType.path(),
                    queryParams
            );
            Map<String, Object> meta = openAlexMapReader.getMap(openAlexResponse, "meta");
            List<Map<String, Object>> rawResults =
                    openAlexMapReader.getMapList(openAlexResponse, "results");

            totalDbResponseTimeMs += openAlexMapReader.getLong(
                    meta,
                    "db_response_time_ms",
                    0L
            );
            totalCostUsd += openAlexMapReader.getDouble(meta, "cost_usd", 0D);

            if (rawResults.isEmpty()) {
                reachedEnd = true;
                break;
            }

            for (Map<String, Object> rawResult : rawResults) {
                String id = openAlexMapReader.getString(rawResult, "id").trim();

                if (
                        id.isBlank()
                                || seenIds.contains(id)
                                || !searchScopeSupport.matchesScopedTopicProfile(rawResult)
                                || !matchesAuthorFilters(
                                rawResult,
                                institutionIds,
                                countryCodes,
                                primaryTopicIds
                        )
                ) {
                    continue;
                }

                seenIds.add(id);
                scopedRawResults.add(rawResult);

                if (scopedRawResults.size() >= requiredScopedResultCount) {
                    break;
                }
            }

            reachedEnd = rawResults.size() < SearchConstants.ENTITY_SCOPE_FETCH_PER_PAGE;
            openAlexPage += 1;
        }

        int safeFromIndex = Math.min(fromIndex, scopedRawResults.size());
        int safeToIndex = Math.min(pageEndExclusive, scopedRawResults.size());
        boolean hasMore = scopedRawResults.size() > pageEndExclusive;
        List<SearchEntitiesResponse.EntityItem> pageResults = mapEntityItems(
                entityType,
                scopedRawResults.subList(safeFromIndex, safeToIndex)
        );

        // totalCount is a lower bound while more raw OpenAlex pages may still contain scoped matches.
        return new SearchEntitiesResponse(
                new SearchEntitiesResponse.Meta(
                        scopedRawResults.size(),
                        page,
                        perPage,
                        totalDbResponseTimeMs,
                        totalCostUsd,
                        entityType.parameterValue(),
                        hasMore,
                        reachedEnd
                ),
                pageResults
        );
    }

    private SearchEntitiesResponse emptyResponse(
            SearchEntityType entityType,
            int page,
            int perPage
    ) {
        return new SearchEntitiesResponse(
                new SearchEntitiesResponse.Meta(
                        0L,
                        page,
                        perPage,
                        0L,
                        0D,
                        entityType.parameterValue(),
                        false,
                        true
                ),
                List.of()
        );
    }

    private SearchEntitiesResponse mapDirectResponse(
            SearchEntityType entityType,
            Map<String, Object> openAlexResponse,
            int fallbackPage,
            int fallbackPerPage
    ) {
        Map<String, Object> meta = openAlexMapReader.getMap(openAlexResponse, "meta");
        List<Map<String, Object>> rawResults =
                openAlexMapReader.getMapList(openAlexResponse, "results");
        List<SearchEntitiesResponse.EntityItem> results = mapEntityItems(entityType, rawResults);
        long totalCount = openAlexMapReader.getLong(meta, "count", 0L);
        int page = openAlexMapReader.getInt(meta, "page", fallbackPage);
        int perPage = openAlexMapReader.getInt(meta, "per_page", fallbackPerPage);

        return new SearchEntitiesResponse(
                new SearchEntitiesResponse.Meta(
                        totalCount,
                        page,
                        perPage,
                        openAlexMapReader.getLong(meta, "db_response_time_ms", 0L),
                        openAlexMapReader.getDouble(meta, "cost_usd", 0D),
                        entityType.parameterValue(),
                        ((long) page * perPage) < totalCount,
                        true
                ),
                results
        );
    }

    private List<SearchEntitiesResponse.EntityItem> mapEntityItems(
            SearchEntityType entityType,
            List<Map<String, Object>> rawResults
    ) {
        List<SearchEntitiesResponse.EntityItem> results = new ArrayList<>();

        for (Map<String, Object> rawResult : rawResults) {
            SearchEntitiesResponse.EntityItem mappedResult = mapEntityItem(
                    entityType,
                    rawResult
            );

            if (mappedResult != null) {
                results.add(mappedResult);
            }
        }

        return results;
    }

    private SearchEntitiesResponse.EntityItem mapEntityItem(
            SearchEntityType entityType,
            Map<String, Object> rawResult
    ) {
        String id = openAlexMapReader.getString(rawResult, "id").trim();
        String displayName = openAlexMapReader.sanitizeDisplayText(
                openAlexMapReader.getString(rawResult, "display_name")
        );

        if (id.isBlank() || displayName == null || displayName.isBlank()) {
            return null;
        }

        if (SearchEntityType.AUTHORS.equals(entityType)) {
            return mapAuthorItem(entityType, rawResult, id, displayName);
        }

        if (SearchEntityType.TOPICS.equals(entityType)) {
            return mapTopicItem(entityType, rawResult, id, displayName);
        }

        return null;
    }

    private SearchEntitiesResponse.EntityItem mapAuthorItem(
            SearchEntityType entityType,
            Map<String, Object> rawResult,
            String id,
            String displayName
    ) {
        List<Map<String, Object>> institutions =
                openAlexMapReader.getMapListFromObject(rawResult.get("last_known_institutions"));
        List<Map<String, Object>> topics =
                openAlexMapReader.getMapListFromObject(rawResult.get("topics"));

        String primaryInstitutionName = readFirstDisplayName(institutions);
        String primaryTopicName = readFirstDisplayName(topics);

        return new SearchEntitiesResponse.EntityItem(
                id,
                entityType.parameterValue(),
                displayName,
                primaryInstitutionName,
                primaryTopicName,
                null,
                null,
                null,
                openAlexMapReader.getLong(rawResult, "works_count", 0L)
        );
    }

    private SearchEntitiesResponse.EntityItem mapTopicItem(
            SearchEntityType entityType,
            Map<String, Object> rawResult,
            String id,
            String displayName
    ) {
        Map<String, Object> subfield = openAlexMapReader.getMap(rawResult, "subfield");
        Map<String, Object> field = openAlexMapReader.getMap(rawResult, "field");
        Map<String, Object> domain = openAlexMapReader.getMap(rawResult, "domain");

        return new SearchEntitiesResponse.EntityItem(
                id,
                entityType.parameterValue(),
                displayName,
                null,
                null,
                normalizeDisplayText(openAlexMapReader.getString(subfield, "display_name")),
                normalizeDisplayText(openAlexMapReader.getString(field, "display_name")),
                normalizeDisplayText(openAlexMapReader.getString(domain, "display_name")),
                openAlexMapReader.getLong(rawResult, "works_count", 0L)
        );
    }

    private String readFirstDisplayName(List<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            String displayName = normalizeDisplayText(
                    openAlexMapReader.getString(item, "display_name")
            );

            if (StringUtils.hasText(displayName)) {
                return displayName;
            }
        }

        return null;
    }
    private String normalizeDisplayText(String value) {
        String normalizedValue = openAlexMapReader.sanitizeDisplayText(value);

        if (!StringUtils.hasText(normalizedValue)) {
            return null;
        }

        return normalizedValue;
    }

    private String buildEntityNameFilter(String query) {
        String normalizedQuery = normalizeFieldSpecificSearchQuery(query);

        if (!StringUtils.hasText(normalizedQuery)) {
            return null;
        }

        return "display_name.search:" + normalizedQuery;
    }

    private boolean hasEntitySearchCriteria(
            SearchEntityType entityType,
            boolean hasSearchQuery,
            List<String> institutionIds,
            List<String> countryCodes,
            List<String> primaryTopicIds,
            List<String> subFieldIds,
            List<String> fieldIds
    ) {
        if (hasSearchQuery) {
            return true;
        }

        if (SearchEntityType.AUTHORS.equals(entityType)) {
            return !institutionIds.isEmpty()
                    || !countryCodes.isEmpty()
                    || !primaryTopicIds.isEmpty();
        }

        if (SearchEntityType.TOPICS.equals(entityType)) {
            return !subFieldIds.isEmpty() || !fieldIds.isEmpty();
        }

        return false;
    }

    private boolean matchesAuthorFilters(
            Map<String, Object> rawResult,
            List<String> institutionIds,
            List<String> countryCodes,
            List<String> primaryTopicIds
    ) {
        List<Map<String, Object>> institutions =
                openAlexMapReader.getMapListFromObject(rawResult.get("last_known_institutions"));

        if (!matchesInstitutionFilter(institutions, institutionIds)) {
            return false;
        }

        if (!matchesCountryFilter(institutions, countryCodes)) {
            return false;
        }

        return matchesPrimaryTopicFilter(
                openAlexMapReader.getMapListFromObject(rawResult.get("topics")),
                primaryTopicIds
        );
    }

    private boolean matchesInstitutionFilter(
            List<Map<String, Object>> institutions,
            List<String> institutionIds
    ) {
        if (institutionIds.isEmpty()) {
            return true;
        }

        for (Map<String, Object> institution : institutions) {
            String normalizedInstitutionId = searchQuerySupport.normalizeEntityValue(
                    openAlexMapReader.getString(institution, "id")
            );

            if (institutionIds.contains(normalizedInstitutionId)) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesCountryFilter(
            List<Map<String, Object>> institutions,
            List<String> countryCodes
    ) {
        if (countryCodes.isEmpty()) {
            return true;
        }

        for (Map<String, Object> institution : institutions) {
            String normalizedCountryCode = searchQuerySupport.normalizeCountryOptionValue(
                    openAlexMapReader.getString(institution, "country_code")
            );

            if (countryCodes.contains(normalizedCountryCode)) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesPrimaryTopicFilter(
            List<Map<String, Object>> topics,
            List<String> primaryTopicIds
    ) {
        if (primaryTopicIds.isEmpty()) {
            return true;
        }

        String primaryTopicId = readFirstNormalizedId(topics);
        return StringUtils.hasText(primaryTopicId) && primaryTopicIds.contains(primaryTopicId);
    }

    private String readFirstNormalizedId(List<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            String normalizedId = searchQuerySupport.normalizeEntityValue(
                    openAlexMapReader.getString(item, "id")
            );

            if (StringUtils.hasText(normalizedId)) {
                return normalizedId;
            }
        }

        return null;
    }

    private String buildMultiValueFilter(String fieldName, List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        return fieldName + ":" + String.join("|", values);
    }

    private String combineFilters(String... filters) {
        List<String> appliedFilters = new ArrayList<>();

        for (String filter : filters) {
            if (StringUtils.hasText(filter)) {
                appliedFilters.add(filter);
            }
        }

        if (appliedFilters.isEmpty()) {
            return null;
        }

        return String.join(",", appliedFilters);
    }

    private String normalizeFieldSpecificSearchQuery(String rawQuery) {
        if (!StringUtils.hasText(rawQuery)) {
            return "";
        }

        return rawQuery
                .trim()
                .replace(',', ' ')
                .replaceAll("\\s+", " ");
    }
}
