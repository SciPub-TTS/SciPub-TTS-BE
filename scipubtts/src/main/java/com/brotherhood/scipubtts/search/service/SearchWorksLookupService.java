package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.common.openalex.OpenAlexCursorSupport;
import com.brotherhood.scipubtts.search.dto.SearchWorksQueryRequest;
import com.brotherhood.scipubtts.search.dto.SearchWorksResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchWorksLookupService {

    // This service is the main search flow:
    // 1. normalize request
    // 2. build OpenAlex query
    // 3. call OpenAlex
    // 4. map response into our DTO
    private final OpenAlexClient openAlexClient;
    private final SearchQuerySupport searchQuerySupport;
    private final SearchFilterBuilder searchFilterBuilder;
    private final SearchWorksMapper searchWorksMapper;
    private final OpenAlexMapReader openAlexMapReader;

    public SearchWorksLookupService(
            OpenAlexClient openAlexClient,
            SearchQuerySupport searchQuerySupport,
            SearchFilterBuilder searchFilterBuilder,
            SearchWorksMapper searchWorksMapper,
            OpenAlexMapReader openAlexMapReader
    ) {
        this.openAlexClient = openAlexClient;
        this.searchQuerySupport = searchQuerySupport;
        this.searchFilterBuilder = searchFilterBuilder;
        this.searchWorksMapper = searchWorksMapper;
        this.openAlexMapReader = openAlexMapReader;
    }

    public SearchWorksResponse searchWorks(SearchWorksQueryRequest request) {
        SearchWorksQueryRequest safeRequest = getSafeRequest(request);
        int page = searchQuerySupport.normalizeWorksPage(safeRequest.getPage());
        int perPage = searchQuerySupport.normalizePerPage(safeRequest.getPerPage());
        String appliedFilter = searchFilterBuilder.build(safeRequest);
        String appliedSort = resolveSort(safeRequest);

        String cursor = OpenAlexCursorSupport.resolveCursorForPage(
                page,
                currentCursor -> fetchNextCursor(
                        safeRequest,
                        perPage,
                        appliedFilter,
                        appliedSort,
                        currentCursor
                )
        );

        if (cursor == null) {
            return emptyResponse(page, perPage, appliedFilter, appliedSort);
        }

        Map<String, String> queryParams = buildOpenAlexQueryParams(
                safeRequest,
                cursor,
                perPage,
                appliedFilter,
                appliedSort
        );

        Map<String, Object> openAlexResponse = openAlexClient.get("/works", queryParams);
        return searchWorksMapper.map(openAlexResponse, appliedFilter, appliedSort, page, perPage);
    }

    private String fetchNextCursor(
            SearchWorksQueryRequest request,
            int perPage,
            String appliedFilter,
            String appliedSort,
            String cursor
    ) {
        Map<String, String> queryParams = buildOpenAlexQueryParams(
                request,
                cursor,
                perPage,
                appliedFilter,
                appliedSort
        );
        Map<String, Object> response = openAlexClient.get("/works", queryParams);
        return openAlexMapReader.getNextCursor(response);
    }

    private SearchWorksResponse emptyResponse(
            int page,
            int perPage,
            String appliedFilter,
            String appliedSort
    ) {
        return new SearchWorksResponse(
                new SearchWorksResponse.Meta(
                        0L,
                        page,
                        perPage,
                        0L,
                        0.0,
                        appliedFilter,
                        appliedSort,
                        null
                ),
                List.of()
        );
    }

    private SearchWorksQueryRequest getSafeRequest(SearchWorksQueryRequest request) {
        if (request == null) {
            return SearchWorksQueryRequest.empty();
        }

        validateExclusiveRangeFilters(request);
        return request;
    }

    private void validateExclusiveRangeFilters(SearchWorksQueryRequest request) {
        validateExactVsRange(
                "Year exact cannot be combined with Year from/to.",
                request.getYearExact(),
                request.getYearFrom(),
                request.getYearTo()
        );
        validateExactVsRange(
                "Citation exact cannot be combined with Citation min/max.",
                request.getCitationExact(),
                request.getCitationMin(),
                request.getCitationMax()
        );
    }

    private void validateExactVsRange(
            String errorMessage,
            Integer exactValue,
            Integer minValue,
            Integer maxValue
    ) {
        boolean hasExactValue = exactValue != null;
        boolean hasRangeValue = minValue != null || maxValue != null;

        if (hasExactValue && hasRangeValue) {
            throw new BusinessException(
                    ErrorCode.INVALID_SEARCH_FILTER_COMBINATION,
                    errorMessage
            );
        }
    }

    private String resolveSort(SearchWorksQueryRequest request) {
        boolean hasSearchQuery = StringUtils.hasText(request.getQuery());

        return searchQuerySupport.resolveSort(
                request.getSortBy(),
                request.getSortDirection(),
                hasSearchQuery
        );
    }

    private Map<String, String> buildOpenAlexQueryParams(
            SearchWorksQueryRequest request,
            String cursor,
            int perPage,
            String appliedFilter,
            String appliedSort
    ) {
        Map<String, String> queryParams = new LinkedHashMap<>();

        // Keyword search is only sent when the user actually typed something.
        if (StringUtils.hasText(request.getQuery())) {
            queryParams.put("search", request.getQuery().trim());
        }

        // Filter string is produced by SearchFilterBuilder.
        if (StringUtils.hasText(appliedFilter)) {
            queryParams.put("filter", appliedFilter);
        }

        queryParams.put("sort", appliedSort);
        queryParams.put("cursor", cursor);
        queryParams.put("per_page", String.valueOf(perPage));
        queryParams.put("select", SearchConstants.WORKS_SELECT_FIELDS);

        return queryParams;
    }
}
