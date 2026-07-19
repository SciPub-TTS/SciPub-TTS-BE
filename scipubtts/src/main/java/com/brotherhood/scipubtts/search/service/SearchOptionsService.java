package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.search.dto.SearchEntityType;
import com.brotherhood.scipubtts.search.dto.response.SearchFilterOptionListResponse;
import com.brotherhood.scipubtts.search.dto.response.SearchFilterOptionsResponse;

public interface SearchOptionsService {
    SearchFilterOptionsResponse getFilterOptions(String keyword, int limit, int page);

    SearchFilterOptionListResponse getFilterOptionPage(
            String filterKey,
            SearchEntityType entityType,
            String keyword,
            int limit,
            int page
    );
}
