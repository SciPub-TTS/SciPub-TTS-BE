package com.brotherhood.scipubtts.search.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class SearchQuerySupport {

    public int normalizeFilterOptionLimit(int limit) {
        if (limit <= 0) {
            return SearchConstants.FILTER_OPTION_LIMIT;
        }

        if (limit > SearchConstants.FILTER_OPTION_LIMIT) {
            return SearchConstants.FILTER_OPTION_LIMIT;
        }

        return limit;
    }

    public int normalizeOptionPage(int page) {
        if (page <= 0) {
            return SearchConstants.DEFAULT_PAGE;
        }

        return page;
    }

    public int normalizeWorksPage(Integer page) {
        if (page == null || page <= 0) {
            return SearchConstants.DEFAULT_PAGE;
        }

        return page;
    }

    public int normalizePerPage(Integer perPage) {
        if (perPage == null || perPage <= 0) {
            return SearchConstants.DEFAULT_WORKS_PER_PAGE;
        }

        if (perPage > SearchConstants.WORKS_PER_PAGE_LIMIT) {
            return SearchConstants.WORKS_PER_PAGE_LIMIT;
        }

        return perPage;
    }

    public int normalizeRecentSearchLimit(int limit) {
        if (limit <= 0) {
            return SearchConstants.DEFAULT_RECENT_SEARCH_LIMIT;
        }

        if (limit > SearchConstants.MAX_RECENT_SEARCH_LIMIT) {
            return SearchConstants.MAX_RECENT_SEARCH_LIMIT;
        }

        return limit;
    }

    public String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }

        return keyword.trim();
    }

    public String normalizeMode(String mode) {
        if (!StringUtils.hasText(mode)) {
            return "range";
        }

        return mode.trim().toLowerCase(Locale.ROOT);
    }

    public String resolveSort(String requestedSort, boolean hasSearchQuery) {
        String defaultSort = hasSearchQuery ? "relevance_score:desc" : "cited_by_count:desc";

        if (!StringUtils.hasText(requestedSort)) {
            return defaultSort;
        }

        String normalizedSort = requestedSort.trim().toLowerCase(Locale.ROOT);

        if ("most cited".equals(normalizedSort)
                || "most_cited".equals(normalizedSort)
                || "cited_by_count:desc".equals(normalizedSort)
                || "trending".equals(normalizedSort)) {
            return "cited_by_count:desc";
        }

        if ("latest".equals(normalizedSort) || "publication_year:desc".equals(normalizedSort)) {
            return "publication_year:desc";
        }

        if ("relevance".equals(normalizedSort) || "relevance_score:desc".equals(normalizedSort)) {
            return defaultSort;
        }

        if (normalizedSort.contains(":")) {
            return normalizedSort;
        }

        return defaultSort;
    }

    public List<String> normalizeTypeValues(List<String> values) {
        List<String> normalized = normalizeStringList(values);
        List<String> result = new ArrayList<>();

        for (String value : normalized) {
            result.add(extractLastSegment(value).toLowerCase(Locale.ROOT));
        }

        return result;
    }

    public List<String> normalizeSubFieldValues(List<String> values) {
        List<String> normalized = normalizeStringList(values);
        List<String> result = new ArrayList<>();

        for (String value : normalized) {
            result.add(extractLastSegment(value));
        }

        return result;
    }

    public List<String> normalizeEntityIds(List<String> values) {
        List<String> normalized = normalizeStringList(values);
        List<String> result = new ArrayList<>();

        for (String value : normalized) {
            result.add(extractLastSegment(value));
        }

        return result;
    }

    public List<String> normalizeCountryValues(List<String> values) {
        List<String> normalized = normalizeStringList(values);
        List<String> result = new ArrayList<>();

        for (String value : normalized) {
            result.add(extractLastSegment(value).toUpperCase(Locale.ROOT));
        }

        return result;
    }

    public List<String> normalizeStringList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        List<String> normalized = new ArrayList<>();

        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                continue;
            }

            String trimmed = value.trim();
            if (!trimmed.isBlank()) {
                normalized.add(trimmed);
            }
        }

        return normalized;
    }

    public String normalizeGroupedValue(String groupBy, String rawKey) {
        if ("type".equals(groupBy)) {
            return extractLastSegment(rawKey).toLowerCase(Locale.ROOT);
        }

        if ("institutions.country_code".equals(groupBy)) {
            return extractLastSegment(rawKey).toUpperCase(Locale.ROOT);
        }

        if ("primary_topic.subfield.id".equals(groupBy)) {
            return extractLastSegment(rawKey);
        }

        return rawKey;
    }

    public String normalizeCountryOptionValue(String rawValue) {
        return extractLastSegment(rawValue).toUpperCase(Locale.ROOT);
    }

    public String extractLastSegment(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        int lastSlash = value.lastIndexOf('/');

        if (lastSlash < 0 || lastSlash == value.length() - 1) {
            return value;
        }

        return value.substring(lastSlash + 1);
    }
}
