package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.search.dto.SearchEntityType;
import com.brotherhood.scipubtts.search.dto.response.SearchSummaryResponse;

public interface SearchSummaryService {
    SearchSummaryResponse getSummary(SearchEntityType entityType);
}
