package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.search.dto.SearchEntityType;
import com.brotherhood.scipubtts.search.dto.response.SearchSummaryResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SearchSummaryService {

    private static final int CACHE_MINUTES = 5;

    private final OpenAlexClient openAlexClient;
    private final OpenAlexMapReader openAlexMapReader;
    private final SearchScopeSupport searchScopeSupport;
    private final Map<SearchEntityType, CachedSummary> cachedSummaries = new HashMap<>();

    public SearchSummaryService(
            OpenAlexClient openAlexClient,
            OpenAlexMapReader openAlexMapReader,
            SearchScopeSupport searchScopeSupport
    ) {
        this.openAlexClient = openAlexClient;
        this.openAlexMapReader = openAlexMapReader;
        this.searchScopeSupport = searchScopeSupport;
    }

    public SearchSummaryResponse getSummary(SearchEntityType entityType) {
        SearchEntityType safeEntityType = getSafeEntityType(entityType);

        CachedSummary cachedSummary = cachedSummaries.get(safeEntityType);
        if (cachedSummary != null && !isCacheExpired(cachedSummary)) {
            return cachedSummary.getResponse();
        }

        SearchSummaryResponse freshSummary = loadFreshSummary(safeEntityType);
        saveToCache(safeEntityType, freshSummary);

        return freshSummary;
    }

    private SearchEntityType getSafeEntityType(SearchEntityType entityType) {
        if (entityType == null) {
            return SearchEntityType.WORKS;
        }

        return entityType;
    }

    private boolean isCacheExpired(CachedSummary cachedSummary) {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(cachedSummary.getExpiredAt());
    }

    private SearchSummaryResponse loadFreshSummary(SearchEntityType entityType) {
        long totalCount = fetchTotalCount(entityType);
        String entityTypeValue = entityType.parameterValue();
        boolean totalCountExact = supportsExactSummaryCount(entityType);

        return new SearchSummaryResponse(
                totalCount,
                entityTypeValue,
                totalCountExact
        );
    }

    private void saveToCache(
            SearchEntityType entityType,
            SearchSummaryResponse response
    ) {
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(CACHE_MINUTES);
        CachedSummary cachedSummary = new CachedSummary(response, expiredAt);

        cachedSummaries.put(entityType, cachedSummary);
    }

    private long fetchTotalCount(SearchEntityType entityType) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("per_page", "1");
        queryParams.put("select", "id");
        String directFilter = searchScopeSupport.getDirectFilter(entityType);

        if (directFilter != null && !directFilter.isBlank()) {
            queryParams.put("filter", directFilter);
        }

        Map<String, Object> response = openAlexClient.get(entityType.path(), queryParams);
        Map<String, Object> meta = openAlexMapReader.getMap(response, "meta");

        return openAlexMapReader.getLong(meta, "count", 0L);
    }

    private boolean supportsExactSummaryCount(SearchEntityType entityType) {
        return !SearchEntityType.AUTHORS.equals(entityType);
    }

    private static class CachedSummary {
        private final SearchSummaryResponse response;
        private final LocalDateTime expiredAt;

        public CachedSummary(
                SearchSummaryResponse response,
                LocalDateTime expiredAt
        ) {
            this.response = response;
            this.expiredAt = expiredAt;
        }

        public SearchSummaryResponse getResponse() {
            return response;
        }

        public LocalDateTime getExpiredAt() {
            return expiredAt;
        }
    }
}
