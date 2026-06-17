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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SearchServiceImpl implements SearchService {

    // Split responsibilities into smaller services so each class stays focused.
    private final SearchSummaryService searchSummaryService;
    private final SearchOptionsService searchOptionsService;
    private final SearchWorksLookupService searchWorksLookupService;
    private final SearchEntityLookupService searchEntityLookupService;
    private final SearchHistoryService searchHistoryService;

    public SearchServiceImpl(
            SearchSummaryService searchSummaryService,
            SearchOptionsService searchOptionsService,
            SearchWorksLookupService searchWorksLookupService,
            SearchEntityLookupService searchEntityLookupService,
            SearchHistoryService searchHistoryService
    ) {
        this.searchSummaryService = searchSummaryService;
        this.searchOptionsService = searchOptionsService;
        this.searchWorksLookupService = searchWorksLookupService;
        this.searchEntityLookupService = searchEntityLookupService;
        this.searchHistoryService = searchHistoryService;
    }

    @Override
    public SearchSummaryResponse getSummary(SearchEntityType entityType) {
        return searchSummaryService.getSummary(entityType);
    }

    @Override
    public SearchFilterOptionsResponse getFilterOptions(String keyword, int limit, int page) {
        return searchOptionsService.getFilterOptions(keyword, limit, page);
    }

    @Override
    public SearchFilterOptionListResponse getFilterOptionPage(String filterKey, String keyword, int limit, int page) {
        return searchOptionsService.getFilterOptionPage(filterKey, keyword, limit, page);
    }

    @Override
    public SearchWorksResponse searchWorks(SearchWorksQueryRequest request) {
        return searchWorksLookupService.searchWorks(request);
    }

    @Override
    public SearchEntitiesResponse searchEntities(
            SearchEntityType entityType,
            SearchEntityQueryRequest request
    ) {
        return searchEntityLookupService.searchEntities(entityType, request);
    }

    @Override
    public List<SearchHistoryItemResponse> getRecentSearches(UUID userId, String keyword, int limit) {
        return searchHistoryService.getRecentSearches(userId, keyword, limit);
    }

    @Override
    public void saveSearchHistory(SearchHistorySaveRequest request) {
        searchHistoryService.saveSearchHistory(request);
    }

    @Override
    public void deleteSearchHistory(UUID userId, String query) {
        searchHistoryService.deleteSearchHistory(userId, query);
    }

    @Override
    public void clearSearchHistory(UUID userId) {
        searchHistoryService.clearSearchHistory(userId);
    }
}
