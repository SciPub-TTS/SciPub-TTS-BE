package com.brotherhood.scipubtts.search.service.impl;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.search.dto.request.SearchWorksQueryRequest;
import com.brotherhood.scipubtts.search.dto.response.SearchWorksResponse;
import com.brotherhood.scipubtts.search.service.SearchConstants;
import com.brotherhood.scipubtts.search.service.SearchFilterBuilder;
import com.brotherhood.scipubtts.search.service.SearchQuerySupport;
import com.brotherhood.scipubtts.search.service.SearchWorksLookupService;
import com.brotherhood.scipubtts.search.service.SearchWorksMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SearchWorksLookupServiceImpl implements SearchWorksLookupService {

    private final OpenAlexClient openAlexClient;
    private final SearchQuerySupport searchQuerySupport;
    private final SearchFilterBuilder searchFilterBuilder;
    private final SearchWorksMapper searchWorksMapper;

    public SearchWorksLookupServiceImpl(
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

    @Override
    public SearchWorksResponse searchWorks(SearchWorksQueryRequest request) {
        // Step 1: make sure request is never null and invalid filter combinations are rejected early.
        SearchWorksQueryRequest safeRequest = getSafeRequest(request);

        // Step 2: normalize paging so OpenAlex always receives valid values.
        int page = searchQuerySupport.normalizeWorksPage(safeRequest.page());
        int perPage = searchQuerySupport.normalizePerPage(safeRequest.perPage());

        // Step 3: build one final filter string from keyword search + advanced filters.
        String appliedFilter = combineFilters(
                buildKeywordFilter(safeRequest.query()),
                searchFilterBuilder.build(safeRequest)
        );

        // Step 4: choose the final sort in one place so controller/service code stays simple.
        String appliedSort = resolveSort(safeRequest);

        // Step 5: convert our request into OpenAlex query parameters.
        Map<String, String> queryParams = buildOpenAlexQueryParams(
                page,
                perPage,
                appliedFilter,
                appliedSort
        );

        // Step 6: call OpenAlex and map the raw response into our response DTO.
        Map<String, Object> openAlexResponse = openAlexClient.get("/works", queryParams);
        return searchWorksMapper.map(openAlexResponse, appliedFilter, appliedSort, page, perPage);
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
                request.yearExact(),
                request.yearFrom(),
                request.yearTo()
        );
        validateExactVsRange(
                "Citation exact cannot be combined with Citation min/max.",
                request.citationExact(),
                request.citationMin(),
                request.citationMax()
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
        boolean hasSearchQuery = StringUtils.hasText(request.query());

        return searchQuerySupport.resolveSort(
                request.sortBy(),
                request.sortDirection(),
                hasSearchQuery
        );
    }

    private Map<String, String> buildOpenAlexQueryParams(
            int page,
            int perPage,
            String appliedFilter,
            String appliedSort
    ) {
        Map<String, String> queryParams = new LinkedHashMap<>();

        if (StringUtils.hasText(appliedFilter)) {
            queryParams.put("filter", appliedFilter);
        }

        queryParams.put("sort", appliedSort);
        queryParams.put("page", String.valueOf(page));
        queryParams.put("per_page", String.valueOf(perPage));
        queryParams.put("select", SearchConstants.WORKS_SELECT_FIELDS);

        return queryParams;
    }

    private String buildKeywordFilter(String rawQuery) {
        String normalizedQuery = normalizeFieldSpecificSearchQuery(rawQuery);

        if (!StringUtils.hasText(normalizedQuery)) {
            return null;
        }

        // Search by title + abstract because this matches the expected FE search experience.
        return "title_and_abstract.search:" + normalizedQuery;
    }

    private String combineFilters(String firstFilter, String secondFilter) {
        boolean hasFirstFilter = StringUtils.hasText(firstFilter);
        boolean hasSecondFilter = StringUtils.hasText(secondFilter);

        if (!hasFirstFilter && !hasSecondFilter) {
            return null;
        }

        if (!hasFirstFilter) {
            return secondFilter;
        }

        if (!hasSecondFilter) {
            return firstFilter;
        }

        return firstFilter + "," + secondFilter;
    }

    private String normalizeFieldSpecificSearchQuery(String rawQuery) {
        if (!StringUtils.hasText(rawQuery)) {
            return "";
        }

        return rawQuery
                .trim()
                .replace(',', ' ')
                .replaceAll("\\s+", " ");
    }
}
