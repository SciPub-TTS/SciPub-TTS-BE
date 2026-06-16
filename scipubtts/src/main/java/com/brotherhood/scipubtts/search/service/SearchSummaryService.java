package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.search.dto.SearchEntityType;
import com.brotherhood.scipubtts.search.dto.response.SearchSummaryResponse;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SearchSummaryService {

    private static final Duration SUMMARY_CACHE_TTL = Duration.ofMinutes(5);

    private final OpenAlexClient openAlexClient;
    private final OpenAlexMapReader openAlexMapReader;
    private final SearchScopeSupport searchScopeSupport;
    private final Map<SearchEntityType, CachedSummary> cachedSummaries =
            new ConcurrentHashMap<>();

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
        SearchEntityType safeEntityType =
                entityType == null ? SearchEntityType.WORKS : entityType;
        CachedSummary currentSummary = cachedSummaries.get(safeEntityType);

        if (currentSummary != null && !currentSummary.isExpired()) {
            return currentSummary.response();
        }

        SearchSummaryResponse freshSummary = new SearchSummaryResponse(
                fetchTotalCount(safeEntityType),
                safeEntityType.parameterValue(),
                supportsExactSummaryCount(safeEntityType)
        );
        cachedSummaries.put(
                safeEntityType,
                new CachedSummary(
                        freshSummary,
                        Instant.now().plus(SUMMARY_CACHE_TTL)
                )
        );

        return freshSummary;
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

    private record CachedSummary(
            SearchSummaryResponse response,
            Instant expiresAt
    ) {
        private boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
