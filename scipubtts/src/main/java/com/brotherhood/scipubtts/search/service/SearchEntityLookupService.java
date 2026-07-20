package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.search.dto.SearchEntityType;
import com.brotherhood.scipubtts.search.dto.request.SearchEntityQueryRequest;
import com.brotherhood.scipubtts.search.dto.response.SearchEntitiesResponse;

public interface SearchEntityLookupService {
    SearchEntitiesResponse searchEntities(
            SearchEntityType entityType,
            SearchEntityQueryRequest request
    );
}
