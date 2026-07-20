package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.search.dto.request.SearchWorksQueryRequest;
import com.brotherhood.scipubtts.search.dto.response.SearchWorksResponse;

public interface SearchWorksLookupService {
    SearchWorksResponse searchWorks(SearchWorksQueryRequest request);
}
