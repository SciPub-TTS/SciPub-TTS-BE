package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.search.dto.SearchFilterOptionsResponse;
import com.brotherhood.scipubtts.search.dto.SearchHistoryItemResponse;
import com.brotherhood.scipubtts.search.dto.SearchHistorySaveRequest;
import com.brotherhood.scipubtts.search.dto.SearchWorksQueryRequest;
import com.brotherhood.scipubtts.search.dto.SearchWorksResponse;
import com.brotherhood.scipubtts.search.entity.SearchHistory;
import com.brotherhood.scipubtts.search.repository.SearchHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class SearchServiceImpl implements SearchService {

    // Maximum number of filter options requested from OpenAlex at one time.
    private static final int FILTER_OPTION_LIMIT = 100;

    // Maximum per_page value accepted for OpenAlex works requests.
    private static final int WORKS_PER_PAGE_LIMIT = 100;

    // First page used by OpenAlex pagination.
    private static final int DEFAULT_PAGE = 1;

    // Number of works returned when the client does not send perPage.
    private static final int DEFAULT_WORKS_PER_PAGE = 20;

    // Number of recent searches returned when the client does not send a limit.
    private static final int DEFAULT_RECENT_SEARCH_LIMIT = 5;

    // Maximum recent-search rows the client is allowed to request.
    private static final int MAX_RECENT_SEARCH_LIMIT = 20;

    // Lowest publication year accepted by the search filters.
    private static final int MIN_YEAR = 1900;

    // Lowest citation count accepted by the search filters.
    private static final int MIN_CITATION = 0;

    // Pattern used to remove HTML tags from OpenAlex text fields.
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("(?is)<[^>]+>");

    // Pattern used to collapse repeated whitespace after text cleanup.
    private static final Pattern MULTI_WHITESPACE_PATTERN = Pattern.compile("\\s+");

    // Fields requested from OpenAlex /works so the response stays small.
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
        String normalizedKeyword = "";
        if (keyword != null) {
            normalizedKeyword = keyword.trim();
        }

        List<SearchFilterOptionsResponse.FacetOption> typeOptions = fetchGroupedWorkOptions("type", normalizedLimit, normalizedPage);
        List<SearchFilterOptionsResponse.FacetOption> subFieldOptions = fetchGroupedWorkOptions("primary_topic.subfield.id", normalizedLimit, normalizedPage);
        List<SearchFilterOptionsResponse.FacetOption> countryOptions;
        if (StringUtils.hasText(normalizedKeyword)) {
            countryOptions = fetchCountryOptions(normalizedKeyword, normalizedLimit, normalizedPage);
        } else {
            countryOptions = fetchGroupedWorkOptions("institutions.country_code", normalizedLimit, normalizedPage);
        }
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
        SearchWorksQueryRequest safeRequest = request;
        if (safeRequest == null) {
            safeRequest = SearchWorksQueryRequest.empty();
        }

        int page = normalizePage(safeRequest.getPage());
        int perPage = normalizePerPage(safeRequest.getPerPage());

        String appliedFilter = buildOpenAlexFilter(safeRequest);

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

        Map<String, Object> openAlexResponse = openAlexClient.get("/works", queryParams);

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
            response.add(mapRecentSearch(recentSearch));
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

    private SearchHistoryItemResponse mapRecentSearch(
            SearchHistoryRepository.RecentSearchProjection recentSearch
    ) {
        String savedAt = null;
        if (recentSearch.getLatestCreatedAt() != null) {
            savedAt = recentSearch.getLatestCreatedAt().toString();
        }

        return new SearchHistoryItemResponse(
                recentSearch.getContent(),
                recentSearch.getContent(),
                savedAt
        );
    }

    private SearchWorksResponse mapWorksResponse(
            Map<String, Object> response,
            String appliedFilter,
            String appliedSort,
            int fallbackPage,
            int fallbackPerPage
    ) {
        Map<String, Object> meta = getMap(response, "meta");
        long totalCount = getLong(meta, "count", 0L);
        int page = getInt(meta, "page", fallbackPage);
        int perPage = getInt(meta, "per_page", fallbackPerPage);
        long dbResponseTimeMs = getLong(meta, "db_response_time_ms", 0L);
        double costUsd = getDouble(meta, "cost_usd", 0.0);

        List<SearchWorksResponse.WorkItem> items = new ArrayList<>();
        List<Map<String, Object>> results = getMapList(response, "results");

        for (Map<String, Object> result : results) {
            Map<String, Object> openAccess = getMap(result, "open_access");
            Map<String, Object> hasContent = getMap(result, "has_content");
            Map<String, Object> primaryTopic = getMap(result, "primary_topic");
            Map<String, Object> subField = getMap(primaryTopic, "subfield");
            Map<String, Object> primaryLocation = getMap(result, "primary_location");
            Map<String, Object> source = getMap(primaryLocation, "source");
            List<Map<String, Object>> authorships = getMapList(result, "authorships");

            items.add(new SearchWorksResponse.WorkItem(
                    getString(result, "id"),
                    sanitizeDisplayText(getString(result, "display_name")),
                    deriveAbstractText(getMap(result, "abstract_inverted_index")),
                    getString(result, "doi"),
                    getInteger(result, "publication_year"),
                    getInteger(result, "cited_by_count"),
                    getBoolean(openAccess, "is_oa"),
                    getBoolean(hasContent, "pdf"),
                    derivePdfUrl(result),
                    deriveHasOrcid(authorships),
                    sanitizeDisplayText(getString(result, "type")),
                    sanitizeDisplayText(getString(primaryTopic, "display_name")),
                    sanitizeDisplayText(getString(subField, "display_name")),
                    getString(source, "id"),
                    sanitizeDisplayText(getString(source, "display_name")),
                    mapAuthorNames(authorships)
            ));
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

    private List<String> mapAuthorNames(List<Map<String, Object>> authorships) {
        List<String> names = new ArrayList<>();

        for (Map<String, Object> authorship : authorships) {
            Map<String, Object> author = getMap(authorship, "author");
            String name = sanitizeDisplayText(getString(author, "display_name"));
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
        String yearMode = normalizeMode(request.getYearMode());

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
        String citationMode = normalizeMode(request.getCitationMode());

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

        List<String> limitedValues = values;
        if (values.size() > FILTER_OPTION_LIMIT) {
            limitedValues = values.subList(0, FILTER_OPTION_LIMIT);
        }

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
        String defaultSort = "cited_by_count:desc";
        if (hasSearchQuery) {
            defaultSort = "relevance_score:desc";
        }

        if (!StringUtils.hasText(requestedSort)) {
            return defaultSort;
        }

        String normalizedSort = requestedSort.trim().toLowerCase(Locale.ROOT);

        if ("most cited".equals(normalizedSort)
                || "most_cited".equals(normalizedSort)
                || "cited_by_count:desc".equals(normalizedSort)
                || "trending".equals(normalizedSort)) {
            return "cited_by_count:desc";
        }

        if ("latest".equals(normalizedSort) || "publication_year:desc".equals(normalizedSort)) {
            return "publication_year:desc";
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

        Map<String, Object> response = openAlexClient.get("/works", queryParams);
        List<Map<String, Object>> groups = getMapList(response, "group_by");

        List<SearchFilterOptionsResponse.FacetOption> options = new ArrayList<>();

        for (Map<String, Object> group : groups) {
            String key = getString(group, "key");
            String label = sanitizeDisplayText(getString(group, "key_display_name"));
            long count = getLong(group, "count", 0L);

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

        Map<String, Object> response = openAlexClient.get(path, queryParams);
        List<Map<String, Object>> results = getMapList(response, "results");

        List<SearchFilterOptionsResponse.EntityOption> options = new ArrayList<>();

        for (Map<String, Object> result : results) {
            String id = getString(result, "id");
            String label = sanitizeDisplayText(getString(result, "display_name"));

            if (id.isBlank() || label.isBlank()) {
                continue;
            }

            Long count = null;
            if (countField != null && result.get(countField) != null) {
                count = getLong(result, countField, 0L);
            }

            options.add(new SearchFilterOptionsResponse.EntityOption(id, label, count));
        }

        return options;
    }

    private List<SearchFilterOptionsResponse.FacetOption> fetchCountryOptions(
            String keyword,
            int limit,
            int page
    ) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("per_page", String.valueOf(limit));
        queryParams.put("page", String.valueOf(page));
        queryParams.put("select", "id,display_name,works_count");

        if (StringUtils.hasText(keyword)) {
            queryParams.put("search", keyword);
        }

        Map<String, Object> response = openAlexClient.get("/countries", queryParams);
        List<Map<String, Object>> results = getMapList(response, "results");
        List<SearchFilterOptionsResponse.FacetOption> options = new ArrayList<>();

        for (Map<String, Object> result : results) {
            String id = getString(result, "id");
            String label = sanitizeDisplayText(getString(result, "display_name"));
            long count = getLong(result, "works_count", 0L);
            String value = normalizeCountryOptionValue(id);

            if (value.isBlank() || label.isBlank()) {
                continue;
            }

            options.add(new SearchFilterOptionsResponse.FacetOption(value, label, count));
        }

        return options;
    }

    private int fetchMaximumCitationCount() {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("per_page", "1");
        queryParams.put("sort", "cited_by_count:desc");
        queryParams.put("select", "cited_by_count");

        Map<String, Object> response = openAlexClient.get("/works", queryParams);
        List<Map<String, Object>> results = getMapList(response, "results");

        if (results.isEmpty()) {
            return 0;
        }

        int maxCitationCount = getInt(results.get(0), "cited_by_count", 0);
        if (maxCitationCount < 0) {
            return 0;
        }

        return maxCitationCount;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return FILTER_OPTION_LIMIT;
        }

        if (limit > FILTER_OPTION_LIMIT) {
            return FILTER_OPTION_LIMIT;
        }

        return limit;
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
            return DEFAULT_WORKS_PER_PAGE;
        }

        if (perPage > WORKS_PER_PAGE_LIMIT) {
            return WORKS_PER_PAGE_LIMIT;
        }

        return perPage;
    }

    private int normalizeRecentSearchLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_RECENT_SEARCH_LIMIT;
        }

        if (limit > MAX_RECENT_SEARCH_LIMIT) {
            return MAX_RECENT_SEARCH_LIMIT;
        }

        return limit;
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

    private String normalizeCountryOptionValue(String rawValue) {
        String value = extractLastSegment(rawValue);
        return value.toUpperCase(Locale.ROOT);
    }

    private String normalizeMode(String mode) {
        if (!StringUtils.hasText(mode)) {
            return "range";
        }

        return mode.trim().toLowerCase(Locale.ROOT);
    }

    private Boolean deriveHasOrcid(List<Map<String, Object>> authorships) {
        for (Map<String, Object> authorship : authorships) {
            Map<String, Object> author = getMap(authorship, "author");
            String orcid = getString(author, "orcid").trim();
            if (!orcid.isBlank()) {
                return true;
            }
        }

        return false;
    }

    private String derivePdfUrl(Map<String, Object> work) {
        Map<String, Object> bestOaLocation = getMap(work, "best_oa_location");
        Map<String, Object> openAccess = getMap(work, "open_access");

        String bestOaPdfUrl = getString(bestOaLocation, "pdf_url").trim();
        if (!bestOaPdfUrl.isBlank()) {
            return bestOaPdfUrl;
        }

        String openAccessUrl = getString(openAccess, "oa_url").trim();
        if (!openAccessUrl.isBlank()) {
            return openAccessUrl;
        }

        String bestOaLandingPageUrl = getString(bestOaLocation, "landing_page_url").trim();
        if (!bestOaLandingPageUrl.isBlank()) {
            return bestOaLandingPageUrl;
        }

        return null;
    }

    private String deriveAbstractText(Map<String, Object> abstractInvertedIndex) {
        if (abstractInvertedIndex == null || abstractInvertedIndex.isEmpty()) {
            return null;
        }

        TreeMap<Integer, String> orderedTokens = new TreeMap<>();
        for (Map.Entry<String, Object> entry : abstractInvertedIndex.entrySet()) {
            String token = entry.getKey();
            List<Object> positions = getObjectList(entry.getValue());

            for (Object positionValue : positions) {
                int position = toInt(positionValue, -1);
                if (position >= 0) {
                    orderedTokens.putIfAbsent(position, token);
                }
            }
        }

        if (orderedTokens.isEmpty()) {
            return null;
        }

        return sanitizeDisplayText(String.join(" ", orderedTokens.values()));
    }

    private String sanitizeDisplayText(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }

        String unescaped = HtmlUtils.htmlUnescape(value);
        String withoutHtmlTags = HTML_TAG_PATTERN.matcher(unescaped).replaceAll(" ");
        return MULTI_WHITESPACE_PATTERN.matcher(withoutHtmlTags).replaceAll(" ").trim();
    }

    private Map<String, Object> getMap(Map<String, Object> source, String key) {
        if (source == null) {
            return new LinkedHashMap<>();
        }

        Object value = source.get(key);
        if (!(value instanceof Map<?, ?> rawMap)) {
            return new LinkedHashMap<>();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (entry.getKey() instanceof String keyName) {
                result.put(keyName, entry.getValue());
            }
        }

        return result;
    }

    private List<Map<String, Object>> getMapList(Map<String, Object> source, String key) {
        if (source == null) {
            return List.of();
        }

        return getMapListFromObject(source.get(key));
    }

    private List<Map<String, Object>> getMapListFromObject(Object value) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (!(value instanceof List<?> rawList)) {
            return result;
        }

        for (Object item : rawList) {
            if (item instanceof Map<?, ?> rawMap) {
                Map<String, Object> itemMap = new LinkedHashMap<>();

                for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                    if (entry.getKey() instanceof String keyName) {
                        itemMap.put(keyName, entry.getValue());
                    }
                }

                result.add(itemMap);
            }
        }

        return result;
    }

    private List<Object> getObjectList(Object value) {
        List<Object> result = new ArrayList<>();

        if (!(value instanceof List<?> rawList)) {
            return result;
        }

        for (Object item : rawList) {
            result.add(item);
        }

        return result;
    }

    private String getString(Map<String, Object> source, String key) {
        if (source == null || source.get(key) == null) {
            return "";
        }

        return String.valueOf(source.get(key));
    }

    private Integer getInteger(Map<String, Object> source, String key) {
        if (source == null || source.get(key) == null) {
            return null;
        }

        return toInt(source.get(key), 0);
    }

    private Boolean getBoolean(Map<String, Object> source, String key) {
        if (source == null || source.get(key) == null) {
            return null;
        }

        Object value = source.get(key);
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        return Boolean.valueOf(String.valueOf(value));
    }

    private int getInt(Map<String, Object> source, String key, int defaultValue) {
        if (source == null || source.get(key) == null) {
            return defaultValue;
        }

        return toInt(source.get(key), defaultValue);
    }

    private long getLong(Map<String, Object> source, String key, long defaultValue) {
        if (source == null || source.get(key) == null) {
            return defaultValue;
        }

        Object value = source.get(key);
        if (value instanceof Number numberValue) {
            return numberValue.longValue();
        }

        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private double getDouble(Map<String, Object> source, String key, double defaultValue) {
        if (source == null || source.get(key) == null) {
            return defaultValue;
        }

        Object value = source.get(key);
        if (value instanceof Number numberValue) {
            return numberValue.doubleValue();
        }

        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private int toInt(Object value, int defaultValue) {
        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
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

