package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.search.dto.SearchWorksQueryRequest;
import com.brotherhood.scipubtts.search.dto.SearchWorksResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SearchWorksLookupService {

    private final OpenAlexClient openAlexClient;
    private final SearchQuerySupport searchQuerySupport;
    private final SearchFilterBuilder searchFilterBuilder;
    private final SearchWorksMapper searchWorksMapper;

    public SearchWorksLookupService(
            OpenAlexClient openAlexClient,
            SearchQuerySupport searchQuerySupport,
            SearchFilterBuilder searchFilterBuilder,
            SearchWorksMapper searchWorksMapper
    ) {
        this.openAlexClient = openAlexClient;
        this.searchQuerySupport = searchQuerySupport;
        this.searchFilterBuilder = searchFilterBuilder;
        this.searchWorksMapper = searchWorksMapper;
    }

    public SearchWorksResponse searchWorks(SearchWorksQueryRequest request) {
        SearchWorksQueryRequest safeRequest = request == null ? SearchWorksQueryRequest.empty() : request;
        int page = searchQuerySupport.normalizeWorksPage(safeRequest.getPage());
        int perPage = searchQuerySupport.normalizePerPage(safeRequest.getPerPage());
        String appliedFilter = searchFilterBuilder.build(safeRequest);

        Map<String, String> queryParams = new LinkedHashMap<>();
        if (StringUtils.hasText(safeRequest.getQuery())) {
            queryParams.put("search", safeRequest.getQuery().trim());
        }
        if (StringUtils.hasText(appliedFilter)) {
            queryParams.put("filter", appliedFilter);
        }

        String appliedSort = searchQuerySupport.resolveSort(
                safeRequest.getSort(),
                StringUtils.hasText(safeRequest.getQuery())
        );

        queryParams.put("sort", appliedSort);
        queryParams.put("page", String.valueOf(page));
        queryParams.put("per_page", String.valueOf(perPage));
        queryParams.put("select", SearchConstants.WORKS_SELECT_FIELDS);

        Map<String, Object> openAlexResponse = openAlexClient.get("/works", queryParams);
        return searchWorksMapper.map(openAlexResponse, appliedFilter, appliedSort, page, perPage);
    }
}
