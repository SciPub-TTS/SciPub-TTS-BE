package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.search.dto.SearchHistoryItemResponse;
import com.brotherhood.scipubtts.search.dto.SearchHistorySaveRequest;
import com.brotherhood.scipubtts.search.entity.SearchHistory;
import com.brotherhood.scipubtts.search.repository.SearchHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final SearchQuerySupport searchQuerySupport;

    public SearchHistoryService(
            SearchHistoryRepository searchHistoryRepository,
            SearchQuerySupport searchQuerySupport
    ) {
        this.searchHistoryRepository = searchHistoryRepository;
        this.searchQuerySupport = searchQuerySupport;
    }

    public List<SearchHistoryItemResponse> getRecentSearches(UUID userId, int limit) {
        if (userId == null) {
            return List.of();
        }

        int normalizedLimit = searchQuerySupport.normalizeRecentSearchLimit(limit);
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

    @Transactional
    public void deleteSearchHistory(UUID userId, String query) {
        if (userId == null || !StringUtils.hasText(query)) {
            return;
        }

        searchHistoryRepository.deleteByUserIdAndContentIgnoreCase(userId, query.trim());
    }

    private SearchHistoryItemResponse mapRecentSearch(SearchHistoryRepository.RecentSearchProjection recentSearch) {
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
}
