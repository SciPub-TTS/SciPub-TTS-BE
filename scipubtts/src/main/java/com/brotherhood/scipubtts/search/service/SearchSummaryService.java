package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.search.dto.SearchSummaryResponse;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SearchSummaryService {

    private static final Duration SUMMARY_CACHE_TTL = Duration.ofMinutes(5);

    private final OpenAlexClient openAlexClient;
    private final OpenAlexMapReader openAlexMapReader;
    private volatile CachedSummary cachedSummary;

    public SearchSummaryService(
            OpenAlexClient openAlexClient,
            OpenAlexMapReader openAlexMapReader
    ) {
        this.openAlexClient = openAlexClient;
        this.openAlexMapReader = openAlexMapReader;
    }

    public SearchSummaryResponse getSummary() {
        CachedSummary currentSummary = cachedSummary;

        if (currentSummary != null && !currentSummary.isExpired()) {
            return currentSummary.response();
        }

        SearchSummaryResponse freshSummary = new SearchSummaryResponse(fetchTotalWorksCount());
        cachedSummary = new CachedSummary(
                freshSummary,
                Instant.now().plus(SUMMARY_CACHE_TTL)
        );

        return freshSummary;
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

    private record CachedSummary(
            SearchSummaryResponse response,
            Instant expiresAt
    ) {
        private boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}