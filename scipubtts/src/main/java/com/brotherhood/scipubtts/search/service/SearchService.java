package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.search.dto.response.SearchFilterOptionsResponse;
import com.brotherhood.scipubtts.search.dto.response.SearchFilterOptionListResponse;
import com.brotherhood.scipubtts.search.dto.response.SearchEntitiesResponse;
import com.brotherhood.scipubtts.search.dto.request.SearchEntityQueryRequest;
import com.brotherhood.scipubtts.search.dto.SearchEntityType;
import com.brotherhood.scipubtts.search.dto.response.SearchHistoryItemResponse;
import com.brotherhood.scipubtts.search.dto.request.SearchHistorySaveRequest;
import com.brotherhood.scipubtts.search.dto.response.SearchSummaryResponse;
import com.brotherhood.scipubtts.search.dto.request.SearchWorksQueryRequest;
import com.brotherhood.scipubtts.search.dto.response.SearchWorksResponse;

import java.util.List;
import java.util.UUID;

public interface SearchService {
    SearchSummaryResponse getSummary(SearchEntityType entityType);

    SearchFilterOptionsResponse getFilterOptions(String keyword, int limit, int page);

    SearchFilterOptionListResponse getFilterOptionPage(String filterKey, String keyword, int limit, int page);

    SearchWorksResponse searchWorks(SearchWorksQueryRequest request);

    SearchEntitiesResponse searchEntities(SearchEntityType entityType, SearchEntityQueryRequest request);

    List<SearchHistoryItemResponse> getRecentSearches(UUID userId, String keyword, int limit);

    void saveSearchHistory(SearchHistorySaveRequest request);

    void deleteSearchHistory(UUID userId, String query);

    void clearSearchHistory(UUID userId);
}

