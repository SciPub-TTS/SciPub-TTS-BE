package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.search.dto.request.SearchHistorySaveRequest;
import com.brotherhood.scipubtts.search.dto.response.SearchHistoryItemResponse;

import java.util.List;
import java.util.UUID;

public interface SearchHistoryService {
    List<SearchHistoryItemResponse> getRecentSearches(UUID userId, String keyword, int limit);

    void saveSearchHistory(SearchHistorySaveRequest request);

    void deleteSearchHistory(UUID userId, String query);

    void clearSearchHistory(UUID userId);
}
