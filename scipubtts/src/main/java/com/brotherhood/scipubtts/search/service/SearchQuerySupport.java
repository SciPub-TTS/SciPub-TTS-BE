package com.brotherhood.scipubtts.search.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

@Component
public class SearchQuerySupport {

    // Keep paging values inside a safe range accepted by our app and OpenAlex.
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
        // When there is a keyword query, OpenAlex relevance is usually the best default.
        // Without a keyword, "most cited" is a more useful default.
        String defaultSort = hasSearchQuery ? "relevance_score:desc" : "cited_by_count:desc";

        if (!StringUtils.hasText(requestedSort)) {
            return defaultSort;
        }

        String normalizedSort = requestedSort.trim().toLowerCase(Locale.ROOT);

        if ("most cited".equals(normalizedSort)
                || "most_cited".equals(normalizedSort)
                || "citation_most_cited".equals(normalizedSort)
                || "cited_by_count:desc".equals(normalizedSort)
                || "trending".equals(normalizedSort)) {
            return "cited_by_count:desc";
        }

        if ("least cited".equals(normalizedSort)
                || "least_cited".equals(normalizedSort)
                || "citation_least_cited".equals(normalizedSort)
                || "cited_by_count:asc".equals(normalizedSort)) {
            return "cited_by_count:asc";
        }

        if ("latest".equals(normalizedSort)
                || "published_latest".equals(normalizedSort)
                || "publication_year:desc".equals(normalizedSort)) {
            return "publication_year:desc";
        }

        if ("oldest".equals(normalizedSort)
                || "published_oldest".equals(normalizedSort)
                || "publication_year:asc".equals(normalizedSort)) {
            return "publication_year:asc";
        }

        if ("trending_keyword".equals(normalizedSort)
                || "trending_topic".equals(normalizedSort)) {
            return defaultSort;
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
        // Type values can arrive as labels or URLs. We only keep the last segment.
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
        // Author, institution, source and award filters use their OpenAlex ids.
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
        // Remove nulls, blanks and duplicates while keeping the original order.
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> normalized = new LinkedHashSet<>();

        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                continue;
            }

            String trimmed = value.trim();
            if (!trimmed.isBlank()) {
                normalized.add(trimmed);
            }
        }

        return new ArrayList<>(normalized);
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
