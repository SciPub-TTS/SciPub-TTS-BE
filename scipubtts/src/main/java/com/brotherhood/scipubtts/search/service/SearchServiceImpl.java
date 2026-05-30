package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.search.dto.SearchFilterOptionsResponse;
import com.brotherhood.scipubtts.search.dto.SearchHistoryItemResponse;
import com.brotherhood.scipubtts.search.dto.SearchHistorySaveRequest;
import com.brotherhood.scipubtts.search.dto.SearchWorksQueryRequest;
import com.brotherhood.scipubtts.search.dto.SearchWorksResponse;
import com.brotherhood.scipubtts.search.entity.SearchHistory;
import com.brotherhood.scipubtts.search.repository.SearchHistoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class SearchServiceImpl implements SearchService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;
    private static final int MIN_LIMIT = 1;

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PER_PAGE = 100;
    private static final int DEFAULT_RECENT_SEARCH_LIMIT = 5;
    private static final int MAX_RECENT_SEARCH_LIMIT = 20;
    private static final int MIN_YEAR = 1900;
    private static final int MIN_CITATION = 0;
    private static final String WORKS_SELECT_FIELDS =
            "id,display_name,abstract_inverted_index,doi,publication_year,cited_by_count,type,primary_topic,primary_location,authorships,open_access,best_oa_location,has_content";

    private final OpenAlexClient openAlexClient;
    private final SearchHistoryRepository searchHistoryRepository;

    public SearchServiceImpl(
            OpenAlexClient openAlexClient,
            SearchHistoryRepository searchHistoryRepository
    ) {
        this.openAlexClient = openAlexClient;
        this.searchHistoryRepository = searchHistoryRepository;
    }

    @Override
    public SearchFilterOptionsResponse getFilterOptions(String keyword, int limit, int page) {
        int normalizedLimit = normalizeLimit(limit);
        int normalizedPage = normalizeOptionPage(page);
        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        List<SearchFilterOptionsResponse.FacetOption> typeOptions = fetchGroupedWorkOptions("type", normalizedLimit, normalizedPage);
        List<SearchFilterOptionsResponse.FacetOption> subFieldOptions = fetchGroupedWorkOptions("primary_topic.subfield.id", normalizedLimit, normalizedPage);
        List<SearchFilterOptionsResponse.FacetOption> countryOptions = fetchGroupedWorkOptions("institutions.country_code", normalizedLimit, normalizedPage);
        List<SearchFilterOptionsResponse.EntityOption> sourceOptions = fetchEntityOptions(
                "/sources",
                normalizedKeyword,
                normalizedLimit,
                normalizedPage,
                "id,display_name,works_count",
                "works_count",
                "works_count:desc"
        );

        List<SearchFilterOptionsResponse.EntityOption> authorOptions = fetchEntityOptions(
                "/authors",
                normalizedKeyword,
                normalizedLimit,
                normalizedPage,
                "id,display_name,works_count",
                "works_count",
                "works_count:desc"
        );

        List<SearchFilterOptionsResponse.EntityOption> institutionOptions = fetchEntityOptions(
                "/institutions",
                normalizedKeyword,
                normalizedLimit,
                normalizedPage,
                "id,display_name,works_count,country_code",
                "works_count",
                "works_count:desc"
        );

        List<SearchFilterOptionsResponse.EntityOption> awardOptions = fetchEntityOptions(
                "/awards",
                normalizedKeyword,
                normalizedLimit,
                normalizedPage,
                "id,display_name",
                null,
                null
        );

        int currentYear = Year.now().getValue();
        int maxCitation = fetchMaximumCitationCount();

        return new SearchFilterOptionsResponse(
                new SearchFilterOptionsResponse.YearRange(MIN_YEAR, currentYear),
                typeOptions,
                new SearchFilterOptionsResponse.ToggleFilter("is_oa", false),
                subFieldOptions,
                authorOptions,
                institutionOptions,
                new SearchFilterOptionsResponse.ToggleFilter("has_content.pdf", false),
                new SearchFilterOptionsResponse.CitationRange(MIN_CITATION, maxCitation),
                countryOptions,
                sourceOptions,
                awardOptions,
                new SearchFilterOptionsResponse.EnumFilter("has_orcid", List.of("is", "is not"), "")
        );
    }

    @Override
    public SearchWorksResponse searchWorks(SearchWorksQueryRequest request) {
        SearchWorksQueryRequest safeRequest = request == null ? new SearchWorksQueryRequest() : request;

        int page = normalizePage(safeRequest.getPage());
        int perPage = normalizePerPage(safeRequest.getPerPage());

        // Flow step 1: convert the UI's 12 filters into one OpenAlex filter string.
        String appliedFilter = buildOpenAlexFilter(safeRequest);

        // Flow step 2: add text search/sort/pagination and call OpenAlex /works.
        Map<String, String> queryParams = new LinkedHashMap<>();
        if (StringUtils.hasText(safeRequest.getQuery())) {
            queryParams.put("search", safeRequest.getQuery().trim());
        }
        if (StringUtils.hasText(appliedFilter)) {
            queryParams.put("filter", appliedFilter);
        }

        String appliedSort = resolveSort(safeRequest.getSort(), StringUtils.hasText(safeRequest.getQuery()));
        queryParams.put("sort", appliedSort);
        queryParams.put("page", String.valueOf(page));
        queryParams.put("per_page", String.valueOf(perPage));
        queryParams.put("select", WORKS_SELECT_FIELDS);

        JsonNode openAlexResponse = openAlexClient.get("/works", queryParams);

        // Flow step 3: map OpenAlex payload into FE-friendly DTO.
        return mapWorksResponse(openAlexResponse, appliedFilter, appliedSort, page, perPage);
    }

    @Override
    public List<SearchHistoryItemResponse> getRecentSearches(UUID userId, int limit) {
        int normalizedLimit = normalizeRecentSearchLimit(limit);
        List<SearchHistoryRepository.RecentSearchProjection> recentSearches =
                searchHistoryRepository.findRecentDistinctSearches(
                        userId,
                        PageRequest.of(0, normalizedLimit)
                );

        List<SearchHistoryItemResponse> response = new ArrayList<>();
        for (SearchHistoryRepository.RecentSearchProjection recentSearch : recentSearches) {
            response.add(new SearchHistoryItemResponse(
                    recentSearch.getContent(),
                    recentSearch.getContent(),
                    recentSearch.getLatestCreatedAt() == null
                            ? null
                            : recentSearch.getLatestCreatedAt().toString()
            ));
        }

        return response;
    }

    @Override
    @Transactional
    public void saveSearchHistory(SearchHistorySaveRequest request) {
        if (request == null || request.getUserId() == null || !StringUtils.hasText(request.getQuery())) {
            return;
        }

        SearchHistory searchHistory = new SearchHistory();
        searchHistory.setContent(request.getQuery().trim());
        searchHistory.setUserId(request.getUserId());

        searchHistoryRepository.save(searchHistory);
    }

    @Override
    @Transactional
    public void deleteSearchHistory(UUID userId, String query) {
        if (!StringUtils.hasText(query)) {
            return;
        }

        String normalizedQuery = query.trim();
        searchHistoryRepository.deleteByUserIdAndContentIgnoreCase(userId, normalizedQuery);
    }

    private SearchWorksResponse mapWorksResponse(
            JsonNode response,
            String appliedFilter,
            String appliedSort,
            int fallbackPage,
            int fallbackPerPage
    ) {
        JsonNode meta = response.path("meta");
        long totalCount = meta.path("count").asLong(0L);
        int page = meta.path("page").asInt(fallbackPage);
        int perPage = meta.path("per_page").asInt(fallbackPerPage);
        long dbResponseTimeMs = meta.path("db_response_time_ms").asLong(0L);
        double costUsd = meta.path("cost_usd").asDouble(0.0);

        List<SearchWorksResponse.WorkItem> items = new ArrayList<>();
        JsonNode results = response.path("results");

        if (results.isArray()) {
            for (JsonNode result : results) {
                items.add(new SearchWorksResponse.WorkItem(
                        result.path("id").asText(null),
                        result.path("display_name").asText(null),
                        deriveAbstractText(result.path("abstract_inverted_index")),
                        result.path("doi").asText(null),
                        nullableInt(result.path("publication_year")),
                        nullableInt(result.path("cited_by_count")),
                        nullableBoolean(result.path("open_access").path("is_oa")),
                        nullableBoolean(result.path("has_content").path("pdf")),
                        derivePdfUrl(result),
                        deriveHasOrcid(result.path("authorships")),
                        result.path("type").asText(null),
                        result.path("primary_topic").path("display_name").asText(null),
                        result.path("primary_topic").path("subfield").path("display_name").asText(null),
                        result.path("primary_location").path("source").path("id").asText(null),
                        result.path("primary_location").path("source").path("display_name").asText(null),
                        mapAuthorNames(result.path("authorships"))
                ));
            }
        }

        SearchWorksResponse.Meta responseMeta = new SearchWorksResponse.Meta(
                totalCount,
                page,
                perPage,
                dbResponseTimeMs,
                costUsd,
                appliedFilter,
                appliedSort
        );

        return new SearchWorksResponse(responseMeta, items);
    }

    private List<String> mapAuthorNames(JsonNode authorships) {
        if (!authorships.isArray()) {
            return List.of();
        }

        List<String> names = new ArrayList<>();

        for (JsonNode authorship : authorships) {
            String name = authorship.path("author").path("display_name").asText("").trim();
            if (!name.isBlank()) {
                names.add(name);
            }
        }

        return names;
    }

    private String buildOpenAlexFilter(SearchWorksQueryRequest request) {
        List<String> filterParts = new ArrayList<>();

        addYearFilter(request, filterParts);
        addOrFilter(filterParts, "type", normalizeTypeValues(request.getType()));
        addBooleanFilter(filterParts, "is_oa", request.getOpenAccess());
        addOrFilter(filterParts, "primary_topic.subfield.id", normalizeSubFieldValues(request.getSubField()));
        addOrFilter(filterParts, "authorships.author.id", normalizeEntityIds(request.getAuthor()));
        addOrFilter(filterParts, "authorships.institutions.id", normalizeEntityIds(request.getInstitution()));
        addBooleanFilter(filterParts, "has_content.pdf", request.getPdf());
        addOrFilter(filterParts, "institutions.country_code", normalizeCountryValues(request.getCountry()));
        addCitationFilter(request, filterParts);
        addOrFilter(filterParts, "primary_location.source.id", normalizeEntityIds(request.getSource()));
        addOrFilter(filterParts, "awards.id", normalizeEntityIds(request.getAward()));
        addOrcidFilter(request.getIndexedByOrcid(), filterParts);

        return String.join(",", filterParts);
    }

    private void addYearFilter(SearchWorksQueryRequest request, List<String> filterParts) {
        String yearMode = normalizeMode(request.getYearMode(), "range");

        if ("exact".equals(yearMode) && request.getYearExact() != null) {
            filterParts.add("publication_year:" + request.getYearExact());
            return;
        }

        Integer yearFrom = request.getYearFrom();
        Integer yearTo = request.getYearTo();

        if (yearFrom != null && yearTo != null) {
            filterParts.add("publication_year:" + yearFrom + "-" + yearTo);
            return;
        }

        if (yearFrom != null) {
            filterParts.add("publication_year:>" + yearFrom);
        }

        if (yearTo != null) {
            filterParts.add("publication_year:<" + yearTo);
        }
    }

    private void addCitationFilter(SearchWorksQueryRequest request, List<String> filterParts) {
        String citationMode = normalizeMode(request.getCitationMode(), "range");

        if ("exact".equals(citationMode) && request.getCitationExact() != null) {
            filterParts.add("cited_by_count:" + request.getCitationExact());
            return;
        }

        Integer citationMin = request.getCitationMin();
        Integer citationMax = request.getCitationMax();

        if (citationMin != null && citationMax != null) {
            filterParts.add("cited_by_count:" + citationMin + "-" + citationMax);
            return;
        }

        if (citationMin != null) {
            filterParts.add("cited_by_count:>" + citationMin);
        }

        if (citationMax != null) {
            filterParts.add("cited_by_count:<" + citationMax);
        }
    }

    private void addBooleanFilter(List<String> filterParts, String field, Boolean value) {
        if (value == null) {
            return;
        }

        filterParts.add(field + ":" + value);
    }

    private void addOrcidFilter(String indexedByOrcid, List<String> filterParts) {
        if (!StringUtils.hasText(indexedByOrcid)) {
            return;
        }

        String normalized = indexedByOrcid.trim().toLowerCase(Locale.ROOT);

        if ("is".equals(normalized)) {
            filterParts.add("has_orcid:true");
            return;
        }

        if ("is not".equals(normalized)) {
            filterParts.add("has_orcid:false");
        }
    }

    private void addOrFilter(List<String> filterParts, String field, List<String> values) {
        if (values.isEmpty()) {
            return;
        }

        List<String> limitedValues = values.size() > MAX_LIMIT
                ? values.subList(0, MAX_LIMIT)
                : values;

        filterParts.add(field + ":" + String.join("|", limitedValues));
    }

    private List<String> normalizeTypeValues(List<String> values) {
        List<String> normalized = normalizeStringList(values);
        List<String> result = new ArrayList<>();

        for (String value : normalized) {
            result.add(extractLastSegment(value).toLowerCase(Locale.ROOT));
        }

        return result;
    }

    private List<String> normalizeSubFieldValues(List<String> values) {
        List<String> normalized = normalizeStringList(values);
        List<String> result = new ArrayList<>();

        for (String value : normalized) {
            result.add(extractLastSegment(value));
        }

        return result;
    }

    private List<String> normalizeEntityIds(List<String> values) {
        List<String> normalized = normalizeStringList(values);
        List<String> result = new ArrayList<>();

        for (String value : normalized) {
            result.add(extractLastSegment(value));
        }

        return result;
    }

    private List<String> normalizeCountryValues(List<String> values) {
        List<String> normalized = normalizeStringList(values);
        List<String> result = new ArrayList<>();

        for (String value : normalized) {
            result.add(extractLastSegment(value).toUpperCase(Locale.ROOT));
        }

        return result;
    }

    private List<String> normalizeStringList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        List<String> normalized = new ArrayList<>();

        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                continue;
            }

            String trimmed = value.trim();
            if (!trimmed.isBlank()) {
                normalized.add(trimmed);
            }
        }

        return normalized;
    }

    private String resolveSort(String requestedSort, boolean hasSearchQuery) {
        String defaultSort = hasSearchQuery ? "relevance_score:desc" : "cited_by_count:desc";

        if (!StringUtils.hasText(requestedSort)) {
            return defaultSort;
        }

        String normalizedSort = requestedSort.trim().toLowerCase(Locale.ROOT);

        if ("most cited".equals(normalizedSort)
                || "most_cited".equals(normalizedSort)
                || "cited_by_count:desc".equals(normalizedSort)) {
            return "cited_by_count:desc";
        }

        if ("latest".equals(normalizedSort) || "publication_year:desc".equals(normalizedSort)) {
            return "publication_year:desc";
        }

        if ("trending".equals(normalizedSort)) {
            return "cited_by_count:desc";
        }

        if ("relevance".equals(normalizedSort) || "relevance_score:desc".equals(normalizedSort)) {
            return defaultSort;
        }

        if (normalizedSort.contains(":")) {
            return normalizedSort;
        }

        return defaultSort;
    }

    private List<SearchFilterOptionsResponse.FacetOption> fetchGroupedWorkOptions(String groupBy, int limit, int page) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("group_by", groupBy);
        queryParams.put("per_page", String.valueOf(limit));
        queryParams.put("page", String.valueOf(page));
        queryParams.put("sort", "count:desc");

        JsonNode response = openAlexClient.get("/works", queryParams);
        JsonNode groups = response.path("group_by");

        if (!groups.isArray()) {
            return List.of();
        }

        List<SearchFilterOptionsResponse.FacetOption> options = new ArrayList<>();

        for (JsonNode group : groups) {
            String key = group.path("key").asText("");
            String label = group.path("key_display_name").asText("");
            long count = group.path("count").asLong(0L);

            if (key.isBlank() || label.isBlank()) {
                continue;
            }

            options.add(new SearchFilterOptionsResponse.FacetOption(
                    normalizeGroupedValue(groupBy, key),
                    label,
                    count
            ));
        }

        return options;
    }

    private List<SearchFilterOptionsResponse.EntityOption> fetchEntityOptions(
            String path,
            String keyword,
            int limit,
            int page,
            String selectFields,
            String countField,
            String defaultSort
    ) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("per_page", String.valueOf(limit));
        queryParams.put("page", String.valueOf(page));
        queryParams.put("select", selectFields);

        if (StringUtils.hasText(keyword)) {
            queryParams.put("search", keyword);
        } else if (StringUtils.hasText(defaultSort)) {
            queryParams.put("sort", defaultSort);
        }

        JsonNode response = openAlexClient.get(path, queryParams);
        JsonNode results = response.path("results");

        if (!results.isArray()) {
            return List.of();
        }

        List<SearchFilterOptionsResponse.EntityOption> options = new ArrayList<>();

        for (JsonNode result : results) {
            String id = result.path("id").asText("");
            String label = result.path("display_name").asText("");

            if (id.isBlank() || label.isBlank()) {
                continue;
            }

            Long count = null;
            if (countField != null && result.has(countField) && !result.get(countField).isNull()) {
                count = result.get(countField).asLong();
            }

            options.add(new SearchFilterOptionsResponse.EntityOption(id, label, count));
        }

        return options;
    }

    private int fetchMaximumCitationCount() {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("per_page", "1");
        queryParams.put("sort", "cited_by_count:desc");
        queryParams.put("select", "cited_by_count");

        JsonNode response = openAlexClient.get("/works", queryParams);
        JsonNode results = response.path("results");

        if (!results.isArray() || results.isEmpty()) {
            return 0;
        }

        int maxCitationCount = results.get(0).path("cited_by_count").asInt(0);
        return Math.max(maxCitationCount, 0);
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(Math.max(limit, MIN_LIMIT), MAX_LIMIT);
    }

    private int normalizeOptionPage(int page) {
        if (page <= 0) {
            return DEFAULT_PAGE;
        }

        return page;
    }

    private int normalizePage(Integer page) {
        if (page == null || page <= 0) {
            return DEFAULT_PAGE;
        }

        return page;
    }

    private int normalizePerPage(Integer perPage) {
        if (perPage == null || perPage <= 0) {
            return DEFAULT_PER_PAGE;
        }

        return Math.min(perPage, MAX_LIMIT);
    }

    private int normalizeRecentSearchLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_RECENT_SEARCH_LIMIT;
        }

        return Math.min(limit, MAX_RECENT_SEARCH_LIMIT);
    }

    private String normalizeGroupedValue(String groupBy, String rawKey) {
        if ("type".equals(groupBy)) {
            return extractLastSegment(rawKey).toLowerCase(Locale.ROOT);
        }

        if ("institutions.country_code".equals(groupBy)) {
            return extractLastSegment(rawKey).toUpperCase(Locale.ROOT);
        }

        if ("primary_topic.subfield.id".equals(groupBy)) {
            return extractLastSegment(rawKey);
        }

        return rawKey;
    }

    private String normalizeMode(String mode, String fallback) {
        if (!StringUtils.hasText(mode)) {
            return fallback;
        }

        return mode.trim().toLowerCase(Locale.ROOT);
    }

    private Boolean deriveHasOrcid(JsonNode authorships) {
        if (!authorships.isArray()) {
            return null;
        }

        for (JsonNode authorship : authorships) {
            String orcid = authorship.path("author").path("orcid").asText("").trim();
            if (!orcid.isBlank()) {
                return true;
            }
        }

        return false;
    }

    private String derivePdfUrl(JsonNode work) {
        String bestOaPdfUrl = work.path("best_oa_location").path("pdf_url").asText("").trim();
        if (!bestOaPdfUrl.isBlank()) {
            return bestOaPdfUrl;
        }

        String openAccessUrl = work.path("open_access").path("oa_url").asText("").trim();
        if (!openAccessUrl.isBlank()) {
            return openAccessUrl;
        }

        String bestOaLandingPageUrl = work.path("best_oa_location").path("landing_page_url").asText("").trim();
        if (!bestOaLandingPageUrl.isBlank()) {
            return bestOaLandingPageUrl;
        }

        return null;
    }

    private String deriveAbstractText(JsonNode abstractInvertedIndex) {
        if (abstractInvertedIndex == null || !abstractInvertedIndex.isObject()) {
            return null;
        }

        TreeMap<Integer, String> orderedTokens = new TreeMap<>();
        Iterator<Map.Entry<String, JsonNode>> fieldIterator = abstractInvertedIndex.fields();
        while (fieldIterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = fieldIterator.next();
            String token = entry.getKey();
            JsonNode positions = entry.getValue();

            if (!positions.isArray()) {
                continue;
            }

            for (JsonNode positionNode : positions) {
                int position = positionNode.asInt(-1);
                if (position >= 0) {
                    orderedTokens.putIfAbsent(position, token);
                }
            }
        }

        if (orderedTokens.isEmpty()) {
            return null;
        }

        return String.join(" ", orderedTokens.values());
    }

    private Integer nullableInt(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        return node.asInt();
    }

    private Boolean nullableBoolean(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        return node.asBoolean();
    }

    private String extractLastSegment(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        int lastSlash = value.lastIndexOf('/');

        if (lastSlash < 0 || lastSlash == value.length() - 1) {
            return value;
        }

        return value.substring(lastSlash + 1);
    }

}

/*
SEARCH_FILE_NOTE
Syntax su dung:
- @Service, @Transactional, helper method, for-loop, if/else.
- Map<String, String> de build query params OpenAlex.
File nay lam gi:
- Chua business logic chinh: build filter, goi OpenAlex, map ket qua, xu ly history.
Flow chay:
- Nhan DTO -> normalize input -> goi OpenAlexClient/repository -> map response -> tra ve.
*/

