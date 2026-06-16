package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.search.dto.request.SearchWorksQueryRequest;
import com.brotherhood.scipubtts.search.dto.response.SearchWorksResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SearchWorksLookupService {

    // This service is the main search flow:
    // normalize request
    // build OpenAlex query
    // call OpenAlex
    // map response into our DTO
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
        SearchWorksQueryRequest safeRequest = getSafeRequest(request);
        int page = searchQuerySupport.normalizeWorksPage(safeRequest.getPage());
        int perPage = searchQuerySupport.normalizePerPage(safeRequest.getPerPage());
        String appliedFilter = combineFilters(
                buildKeywordFilter(safeRequest.getQuery()),
                searchFilterBuilder.build(safeRequest)
        );
        String appliedSort = resolveSort(safeRequest);
        Map<String, String> queryParams = buildOpenAlexQueryParams(
                safeRequest,
                page,
                perPage,
                appliedFilter,
                appliedSort
        );

        Map<String, Object> openAlexResponse = openAlexClient.get("/works", queryParams);
//        String normalizedTrendingMode = searchQuerySupport.normalizeTrendingMode(safeRequest.getTrendingMode());
//        if (!"none".equals(normalizedTrendingMode)) {
//            openAlexResponse = applyTrendingRanking(openAlexResponse, normalizedTrendingMode);
//        }
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
            int page,
            int perPage,
            String appliedFilter,
            String appliedSort
    ) {
        Map<String, String> queryParams = new LinkedHashMap<>();

        // Filter string is produced by SearchFilterBuilder.
        if (StringUtils.hasText(appliedFilter)) {
            queryParams.put("filter", appliedFilter);
        }

        // These values are always sent so the OpenAlex response is predictable.
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
