package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.search.dto.response.SearchHistoryItemResponse;
import com.brotherhood.scipubtts.search.dto.request.SearchHistorySaveRequest;
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

    public List<SearchHistoryItemResponse> getRecentSearches(UUID userId, String keyword, int limit) {
        if (userId == null) {
            return List.of();
        }

        int normalizedLimit = searchQuerySupport.normalizeRecentSearchLimit(limit);
        String normalizedKeyword = normalizeKeyword(keyword);
        List<SearchHistoryRepository.RecentSearchProjection> recentSearches =
                searchHistoryRepository.findRecentDistinctSearches(
                        userId,
                        normalizedKeyword,
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
        if (request == null || !StringUtils.hasText(request.query())) {
            return;
        }

        if (request.userId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String normalizedQuery = request.query().trim();
        searchHistoryRepository.deleteByUserIdAndContentIgnoreCase(request.userId(), normalizedQuery);

        SearchHistory searchHistory = new SearchHistory();
        searchHistory.setContent(normalizedQuery);
        searchHistory.setUserId(request.userId());

        searchHistoryRepository.save(searchHistory);
    }

    @Transactional
    public void deleteSearchHistory(UUID userId, String query) {
        if (!StringUtils.hasText(query)) {
            return;
        }

        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        searchHistoryRepository.deleteByUserIdAndContentIgnoreCase(userId, query.trim());
    }

    @Transactional
    public void clearSearchHistory(UUID userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        searchHistoryRepository.deleteByUserId(userId);
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

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : "";
    }
}
