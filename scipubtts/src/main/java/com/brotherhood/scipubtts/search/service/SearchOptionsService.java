package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.search.dto.SearchFilterOptionsResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SearchOptionsService {

    private static final Duration DEFAULT_FILTER_OPTIONS_CACHE_TTL = Duration.ofMinutes(5);

    private final OpenAlexClient openAlexClient;
    private final SearchQuerySupport searchQuerySupport;
    private final OpenAlexMapReader openAlexMapReader;
    private final Map<String, CachedFilterOptions> defaultFilterOptionsCache = new ConcurrentHashMap<>();

    public SearchOptionsService(
            OpenAlexClient openAlexClient,
            SearchQuerySupport searchQuerySupport,
            OpenAlexMapReader openAlexMapReader
    ) {
        this.openAlexClient = openAlexClient;
        this.searchQuerySupport = searchQuerySupport;
        this.openAlexMapReader = openAlexMapReader;
    }

    public SearchFilterOptionsResponse getFilterOptions(String keyword, int limit, int page) {
        int normalizedLimit = searchQuerySupport.normalizeFilterOptionLimit(limit);
        int normalizedPage = searchQuerySupport.normalizeOptionPage(page);
        String normalizedKeyword = searchQuerySupport.normalizeKeyword(keyword);

        if (StringUtils.hasText(normalizedKeyword)) {
            return buildFilterOptionsResponse(normalizedKeyword, normalizedLimit, normalizedPage);
        }

        return getDefaultFilterOptions(normalizedLimit, normalizedPage);
    }

    private SearchFilterOptionsResponse getDefaultFilterOptions(int limit, int page) {
        String cacheKey = buildDefaultCacheKey(limit, page);
        CachedFilterOptions cachedFilterOptions = defaultFilterOptionsCache.get(cacheKey);

        if (cachedFilterOptions != null && !cachedFilterOptions.isExpired()) {
            return cachedFilterOptions.getResponse();
        }

        SearchFilterOptionsResponse freshResponse = buildFilterOptionsResponse("", limit, page);
        defaultFilterOptionsCache.put(
                cacheKey,
                new CachedFilterOptions(
                        freshResponse,
                        Instant.now().plus(DEFAULT_FILTER_OPTIONS_CACHE_TTL)
                )
        );

        return freshResponse;
    }

    private SearchFilterOptionsResponse buildFilterOptionsResponse(
            String keyword,
            int limit,
            int page
    ) {
        List<SearchFilterOptionsResponse.FacetOption> typeOptions =
                fetchGroupedWorkOptions("type", limit, page);
        List<SearchFilterOptionsResponse.FacetOption> subFieldOptions =
                fetchGroupedWorkOptions("primary_topic.subfield.id", limit, page);
        List<SearchFilterOptionsResponse.FacetOption> countryOptions =
                fetchScopedFacetOptions("institutions.country_code", keyword, limit, page);

        List<SearchFilterOptionsResponse.EntityOption> sourceOptions = fetchScopedEntityOptions(
                "primary_location.source.id",
                keyword,
                limit,
                page
        );

        List<SearchFilterOptionsResponse.EntityOption> authorOptions = fetchScopedEntityOptions(
                "authorships.author.id",
                keyword,
                limit,
                page
        );

        List<SearchFilterOptionsResponse.EntityOption> institutionOptions = fetchScopedEntityOptions(
                "authorships.institutions.id",
                keyword,
                limit,
                page
        );

        List<SearchFilterOptionsResponse.EntityOption> awardOptions = fetchScopedEntityOptions(
                "awards.id",
                keyword,
                limit,
                page
        );

        return new SearchFilterOptionsResponse(
                fetchTotalWorksCount(),
                new SearchFilterOptionsResponse.YearRange(SearchConstants.MIN_YEAR, Year.now().getValue()),
                typeOptions,
                new SearchFilterOptionsResponse.ToggleFilter("is_oa", false),
                subFieldOptions,
                authorOptions,
                institutionOptions,
                new SearchFilterOptionsResponse.ToggleFilter("has_content.pdf", false),
                new SearchFilterOptionsResponse.CitationRange(SearchConstants.MIN_CITATION, fetchMaximumCitationCount()),
                countryOptions,
                sourceOptions,
                awardOptions,
                new SearchFilterOptionsResponse.EnumFilter("has_orcid", List.of("is", "is not"), "")
        );
    }

    private List<SearchFilterOptionsResponse.FacetOption> fetchGroupedWorkOptions(String groupBy, int limit, int page) {
        Map<String, String> queryParams = createPagedQueryParams(limit, page);
        queryParams.put("filter", SearchConstants.WORKS_SCOPE_FILTER);
        queryParams.put("group_by", groupBy);
        queryParams.put("sort", "count:desc");

        Map<String, Object> response = openAlexClient.get("/works", queryParams);
        List<Map<String, Object>> groups = openAlexMapReader.getMapList(response, "group_by");
        List<SearchFilterOptionsResponse.FacetOption> options = new ArrayList<>();

        for (Map<String, Object> group : groups) {
            String key = openAlexMapReader.getString(group, "key");
            String label = openAlexMapReader.sanitizeDisplayText(
                    openAlexMapReader.getString(group, "key_display_name")
            );
            long count = openAlexMapReader.getLong(group, "count", 0L);

            if (key.isBlank() || label.isBlank()) {
                continue;
            }

            options.add(new SearchFilterOptionsResponse.FacetOption(
                    searchQuerySupport.normalizeGroupedValue(groupBy, key),
                    label,
                    count
            ));
        }

        return options;
    }

    private List<SearchFilterOptionsResponse.FacetOption> fetchScopedFacetOptions(
            String groupBy,
            String keyword,
            int limit,
            int page
    ) {
        Map<String, String> queryParams = createScopedGroupedQueryParams(groupBy, limit, page);
        Map<String, Object> response = openAlexClient.get("/works", queryParams);
        List<Map<String, Object>> groups = openAlexMapReader.getMapList(response, "group_by");
        List<SearchFilterOptionsResponse.FacetOption> options = new ArrayList<>();

        for (Map<String, Object> group : groups) {
            String key = openAlexMapReader.getString(group, "key");
            String label = openAlexMapReader.sanitizeDisplayText(
                    openAlexMapReader.getString(group, "key_display_name")
            );
            long count = openAlexMapReader.getLong(group, "count", 0L);

            if (key.isBlank() || label.isBlank() || !matchesKeyword(label, keyword)) {
                continue;
            }

            options.add(new SearchFilterOptionsResponse.FacetOption(
                    searchQuerySupport.normalizeGroupedValue(groupBy, key),
                    label,
                    count
            ));
        }

        return options;
    }

    private List<SearchFilterOptionsResponse.EntityOption> fetchScopedEntityOptions(
            String groupBy,
            String keyword,
            int limit,
            int page
    ) {
        Map<String, String> queryParams = createScopedGroupedQueryParams(groupBy, limit, page);
        Map<String, Object> response = openAlexClient.get("/works", queryParams);
        List<Map<String, Object>> groups = openAlexMapReader.getMapList(response, "group_by");
        List<SearchFilterOptionsResponse.EntityOption> options = new ArrayList<>();

        for (Map<String, Object> group : groups) {
            String id = openAlexMapReader.getString(group, "key");
            String label = openAlexMapReader.sanitizeDisplayText(
                    openAlexMapReader.getString(group, "key_display_name")
            );
            long count = openAlexMapReader.getLong(group, "count", 0L);

            if (id.isBlank() || label.isBlank() || !matchesKeyword(label, keyword)) {
                continue;
            }

            options.add(new SearchFilterOptionsResponse.EntityOption(id, label, count));
        }

        return options;
    }

    private long fetchTotalWorksCount() {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("filter", SearchConstants.WORKS_SCOPE_FILTER);
        queryParams.put("per_page", "1");
        queryParams.put("select", "id");

        Map<String, Object> response = openAlexClient.get("/works", queryParams);
        Map<String, Object> meta = openAlexMapReader.getMap(response, "meta");

        return openAlexMapReader.getLong(meta, "count", 0L);
    }

    private int fetchMaximumCitationCount() {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("filter", SearchConstants.WORKS_SCOPE_FILTER);
        queryParams.put("per_page", "1");
        queryParams.put("sort", "cited_by_count:desc");
        queryParams.put("select", "cited_by_count");

        Map<String, Object> response = openAlexClient.get("/works", queryParams);
        List<Map<String, Object>> results = openAlexMapReader.getMapList(response, "results");

        if (results.isEmpty()) {
            return 0;
        }

        return Math.max(openAlexMapReader.getInt(results.getFirst(), "cited_by_count", 0), 0);
    }

    private Map<String, String> createPagedQueryParams(int limit, int page) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("per_page", String.valueOf(limit));
        queryParams.put("page", String.valueOf(page));
        return queryParams;
    }

    private Map<String, String> createScopedGroupedQueryParams(String groupBy, int limit, int page) {
        Map<String, String> queryParams = createPagedQueryParams(limit, page);
        queryParams.put("filter", SearchConstants.WORKS_SCOPE_FILTER);
        queryParams.put("group_by", groupBy);
        queryParams.put("sort", "count:desc");
        return queryParams;
    }

    private boolean matchesKeyword(String label, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }

        return label.toLowerCase().contains(keyword.trim().toLowerCase());
    }

    private String buildDefaultCacheKey(int limit, int page) {
        return limit + ":" + page;
    }

    private static class CachedFilterOptions {
        private final SearchFilterOptionsResponse response;
        private final Instant expiresAt;

        private CachedFilterOptions(SearchFilterOptionsResponse response, Instant expiresAt) {
            this.response = response;
            this.expiresAt = expiresAt;
        }

        private SearchFilterOptionsResponse getResponse() {
            return response;
        }

        private boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
