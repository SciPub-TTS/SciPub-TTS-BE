package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.search.dto.SearchFilterOptionsResponse;
import com.brotherhood.scipubtts.search.dto.SearchHistoryItemResponse;
import com.brotherhood.scipubtts.search.dto.SearchHistorySaveRequest;
import com.brotherhood.scipubtts.search.dto.SearchWorksQueryRequest;
import com.brotherhood.scipubtts.search.dto.SearchWorksResponse;

import java.util.List;
import java.util.UUID;

public interface SearchService {
    SearchFilterOptionsResponse getFilterOptions(String keyword, int limit, int page);

    SearchWorksResponse searchWorks(SearchWorksQueryRequest request);

    List<SearchHistoryItemResponse> getRecentSearches(UUID userId, int limit);

    void saveSearchHistory(SearchHistorySaveRequest request);

    void deleteSearchHistory(UUID userId, String query);
}

