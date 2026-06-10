package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.search.dto.SearchFilterOptionsResponse;
import com.brotherhood.scipubtts.search.dto.SearchHistoryItemResponse;
import com.brotherhood.scipubtts.search.dto.SearchHistorySaveRequest;
import com.brotherhood.scipubtts.search.dto.SearchWorksQueryRequest;
import com.brotherhood.scipubtts.search.dto.SearchWorksResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SearchServiceImpl implements SearchService {

    // Split responsibilities into smaller services so each class stays focused.
    private final SearchOptionsService searchOptionsService;
    private final SearchWorksLookupService searchWorksLookupService;
    private final SearchHistoryService searchHistoryService;

    public SearchServiceImpl(
            SearchOptionsService searchOptionsService,
            SearchWorksLookupService searchWorksLookupService,
            SearchHistoryService searchHistoryService
    ) {
        this.searchOptionsService = searchOptionsService;
        this.searchWorksLookupService = searchWorksLookupService;
        this.searchHistoryService = searchHistoryService;
    }

    @Override
    public SearchFilterOptionsResponse getFilterOptions(String keyword, int limit, int page) {
        return searchOptionsService.getFilterOptions(keyword, limit, page);
    }

    @Override
    public SearchWorksResponse searchWorks(SearchWorksQueryRequest request) {
        return searchWorksLookupService.searchWorks(request);
    }

    @Override
    public List<SearchHistoryItemResponse> getRecentSearches(UUID userId, int limit) {
        return searchHistoryService.getRecentSearches(userId, limit);
    }

    @Override
    public void saveSearchHistory(SearchHistorySaveRequest request) {
        searchHistoryService.saveSearchHistory(request);
    }

    @Override
    public void deleteSearchHistory(UUID userId, String query) {
        searchHistoryService.deleteSearchHistory(userId, query);
    }
}
