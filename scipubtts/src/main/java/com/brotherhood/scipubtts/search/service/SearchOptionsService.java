package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.search.dto.response.SearchFilterOptionListResponse;
import com.brotherhood.scipubtts.search.dto.response.SearchFilterOptionsResponse;
import com.brotherhood.scipubtts.search.dto.SearchEntityType;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
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
    private static final int KEYWORD_OPTION_SCAN_PAGE_LIMIT = 20;

    private final OpenAlexClient openAlexClient;
    private final SearchQuerySupport searchQuerySupport;
    private final OpenAlexMapReader openAlexMapReader;
    private final SearchSummaryService searchSummaryService;
    private final Map<String, CachedFilterOptions> defaultFilterOptionsCache = new ConcurrentHashMap<>();

    public SearchOptionsService(
            OpenAlexClient openAlexClient,
            SearchQuerySupport searchQuerySupport,
            OpenAlexMapReader openAlexMapReader,
            SearchSummaryService searchSummaryService
    ) {
        this.openAlexClient = openAlexClient;
        this.searchQuerySupport = searchQuerySupport;
        this.openAlexMapReader = openAlexMapReader;
        this.searchSummaryService = searchSummaryService;
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

    public SearchFilterOptionListResponse getFilterOptionPage(
            String filterKey,
            String keyword,
            int limit,
            int page
    ) {
        int normalizedLimit = searchQuerySupport.normalizeFilterOptionLimit(limit);
        int normalizedPage = searchQuerySupport.normalizeOptionPage(page);
        String normalizedKeyword = searchQuerySupport.normalizeKeyword(keyword);
        String normalizedFilterKey = filterKey == null ? "" : filterKey.trim();

        return switch (normalizedFilterKey) {
            case "type" -> buildFacetFilterOptionPage(
                    normalizedFilterKey,
                    "type",
                    normalizedKeyword,
                    normalizedLimit,
                    normalizedPage,
                    false
            );
            case "subField" -> buildFacetFilterOptionPage(
                    normalizedFilterKey,
                    "primary_topic.subfield.id",
                    normalizedKeyword,
                    normalizedLimit,
                    normalizedPage,
                    false
            );
            case "country" -> buildFacetFilterOptionPage(
                    normalizedFilterKey,
                    "institutions.country_code",
                    normalizedKeyword,
                    normalizedLimit,
                    normalizedPage,
                    true
            );
            case "author" -> buildEntityFilterOptionPage(
                    normalizedFilterKey,
                    "authorships.author.id",
                    normalizedKeyword,
                    normalizedLimit,
                    normalizedPage
            );
            case "institution" -> buildEntityFilterOptionPage(
                    normalizedFilterKey,
                    "authorships.institutions.id",
                    normalizedKeyword,
                    normalizedLimit,
                    normalizedPage
            );
            case "source" -> buildEntityFilterOptionPage(
                    normalizedFilterKey,
                    "primary_location.source.id",
                    normalizedKeyword,
                    normalizedLimit,
                    normalizedPage
            );
            case "award" -> buildEntityFilterOptionPage(
                    normalizedFilterKey,
                    "awards.id",
                    normalizedKeyword,
                    normalizedLimit,
                    normalizedPage
            );
            default -> throw new BusinessException(ErrorCode.OPENALEX_ENTITY_NOT_FOUND);
        };
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
                fetchGroupedWorkOptions("type", keyword, limit, page);
        List<SearchFilterOptionsResponse.FacetOption> subFieldOptions =
                fetchGroupedWorkOptions("primary_topic.subfield.id", keyword, limit, page);
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
                searchSummaryService.getSummary(SearchEntityType.WORKS).totalCount(),
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

    private List<SearchFilterOptionsResponse.FacetOption> fetchGroupedWorkOptions(
            String groupBy,
            String keyword,
            int limit,
            int page
    ) {
        if (!StringUtils.hasText(keyword)) {
            return loadGroupedWorkOptionPage(groupBy, limit, page).options();
        }

        return collectKeywordMatchedOptions(
                keyword,
                limit,
                page,
                sourcePage -> loadGroupedWorkOptionPage(groupBy, limit, sourcePage),
                SearchFilterOptionsResponse.FacetOption::label
        );
    }

    private SearchFilterOptionListResponse buildFacetFilterOptionPage(
            String filterKey,
            String groupBy,
            String keyword,
            int limit,
            int page,
            boolean scoped
    ) {
        List<SearchFilterOptionsResponse.FacetOption> options =
                scoped
                        ? fetchScopedFacetOptions(groupBy, keyword, limit, page)
                        : fetchGroupedWorkOptions(groupBy, keyword, limit, page);

        return new SearchFilterOptionListResponse(
                filterKey,
                mapFacetOptions(options)
        );
    }

    private SearchFilterOptionListResponse buildEntityFilterOptionPage(
            String filterKey,
            String groupBy,
            String keyword,
            int limit,
            int page
    ) {
        List<SearchFilterOptionsResponse.EntityOption> options =
                fetchScopedEntityOptions(groupBy, keyword, limit, page);

        return new SearchFilterOptionListResponse(
                filterKey,
                mapEntityOptions(options)
        );
    }

    private List<SearchFilterOptionsResponse.FacetOption> fetchScopedFacetOptions(
            String groupBy,
            String keyword,
            int limit,
            int page
    ) {
        if (!StringUtils.hasText(keyword)) {
            return loadScopedFacetOptionPage(groupBy, limit, page).options();
        }

        return collectKeywordMatchedOptions(
                keyword,
                limit,
                page,
                sourcePage -> loadScopedFacetOptionPage(groupBy, limit, sourcePage),
                SearchFilterOptionsResponse.FacetOption::label
        );
    }

    private List<SearchFilterOptionsResponse.EntityOption> fetchScopedEntityOptions(
            String groupBy,
            String keyword,
            int limit,
            int page
    ) {
        if (!StringUtils.hasText(keyword)) {
            return loadScopedEntityOptionPage(groupBy, limit, page).options();
        }

        return collectKeywordMatchedOptions(
                keyword,
                limit,
                page,
                sourcePage -> loadScopedEntityOptionPage(groupBy, limit, sourcePage),
                SearchFilterOptionsResponse.EntityOption::label
        );
    }

    private OptionPage<SearchFilterOptionsResponse.FacetOption> loadGroupedWorkOptionPage(
            String groupBy,
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

            if (key.isBlank() || label.isBlank()) {
                continue;
            }

            options.add(new SearchFilterOptionsResponse.FacetOption(
                    searchQuerySupport.normalizeGroupedValue(groupBy, key),
                    label,
                    count
            ));
        }

        return new OptionPage<>(options, groups.size() >= limit);
    }

    private OptionPage<SearchFilterOptionsResponse.FacetOption> loadScopedFacetOptionPage(
            String groupBy,
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

            if (key.isBlank() || label.isBlank()) {
                continue;
            }

            options.add(new SearchFilterOptionsResponse.FacetOption(
                    searchQuerySupport.normalizeGroupedValue(groupBy, key),
                    label,
                    count
            ));
        }

        return new OptionPage<>(options, groups.size() >= limit);
    }

    private OptionPage<SearchFilterOptionsResponse.EntityOption> loadScopedEntityOptionPage(
            String groupBy,
            int limit,
            int page
    ) {
        Map<String, String> queryParams = createScopedGroupedQueryParams(groupBy, limit, page);
        Map<String, Object> response = openAlexClient.get("/works", queryParams);
        List<Map<String, Object>> groups = openAlexMapReader.getMapList(response, "group_by");
        List<SearchFilterOptionsResponse.EntityOption> options = new ArrayList<>();

        for (Map<String, Object> group : groups) {
            String value = searchQuerySupport.normalizeEntityValue(
                    openAlexMapReader.getString(group, "key")
            );
            String label = openAlexMapReader.sanitizeDisplayText(
                    openAlexMapReader.getString(group, "key_display_name")
            );
            long count = openAlexMapReader.getLong(group, "count", 0L);

            if (value.isBlank() || label.isBlank()) {
                continue;
            }

            options.add(new SearchFilterOptionsResponse.EntityOption(value, label, count));
        }

        return new OptionPage<>(options, groups.size() >= limit);
    }

    private List<SearchFilterOptionListResponse.OptionItem> mapFacetOptions(
            List<SearchFilterOptionsResponse.FacetOption> options
    ) {
        List<SearchFilterOptionListResponse.OptionItem> mappedOptions = new ArrayList<>();

        for (SearchFilterOptionsResponse.FacetOption option : options) {
            mappedOptions.add(new SearchFilterOptionListResponse.OptionItem(
                    option.value(),
                    option.label(),
                    option.count()
            ));
        }

        return mappedOptions;
    }

    private List<SearchFilterOptionListResponse.OptionItem> mapEntityOptions(
            List<SearchFilterOptionsResponse.EntityOption> options
    ) {
        List<SearchFilterOptionListResponse.OptionItem> mappedOptions = new ArrayList<>();

        for (SearchFilterOptionsResponse.EntityOption option : options) {
            mappedOptions.add(new SearchFilterOptionListResponse.OptionItem(
                    option.value(),
                    option.label(),
                    option.count()
            ));
        }

        return mappedOptions;
    }

    private <T> List<T> collectKeywordMatchedOptions(
            String keyword,
            int limit,
            int page,
            java.util.function.IntFunction<OptionPage<T>> pageLoader,
            java.util.function.Function<T, String> labelExtractor
    ) {
        int offset = Math.max(page - 1, 0) * limit;
        int targetMatchCount = offset + limit;
        List<T> matchedOptions = new ArrayList<>();

        for (int sourcePage = 1; sourcePage <= KEYWORD_OPTION_SCAN_PAGE_LIMIT; sourcePage++) {
            OptionPage<T> loadedPage = pageLoader.apply(sourcePage);

            for (T option : loadedPage.options()) {
                if (matchesKeyword(labelExtractor.apply(option), keyword)) {
                    matchedOptions.add(option);
                }
            }

            if (matchedOptions.size() >= targetMatchCount || !loadedPage.hasMorePages()) {
                break;
            }
        }

        if (offset >= matchedOptions.size()) {
            return List.of();
        }

        int toIndex = Math.min(targetMatchCount, matchedOptions.size());
        return new ArrayList<>(matchedOptions.subList(offset, toIndex));
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

    private record OptionPage<T>(List<T> options, boolean hasMorePages) {
    }
}
