package com.brotherhood.scipubtts.search.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

@Component
public class SearchQuerySupport {

    public int normalizeFilterOptionLimit(int limit) {
        return normalizePositiveInt(limit, SearchConstants.FILTER_OPTION_LIMIT, SearchConstants.FILTER_OPTION_LIMIT);
    }

    public int normalizeOptionPage(int page) {
        return normalizePositiveInt(page, SearchConstants.DEFAULT_PAGE, Integer.MAX_VALUE);
    }

    public int normalizeWorksPage(Integer page) {
        if (page == null) {
            return SearchConstants.DEFAULT_PAGE;
        }

        return normalizePositiveInt(page, SearchConstants.DEFAULT_PAGE, Integer.MAX_VALUE);
    }

    public int normalizePerPage(Integer perPage) {
        if (perPage == null) {
            return SearchConstants.DEFAULT_WORKS_PER_PAGE;
        }

        return normalizePositiveInt(
                perPage,
                SearchConstants.DEFAULT_WORKS_PER_PAGE,
                SearchConstants.WORKS_PER_PAGE_LIMIT
        );
    }

    public int normalizeRecentSearchLimit(int limit) {
        return normalizePositiveInt(
                limit,
                SearchConstants.DEFAULT_RECENT_SEARCH_LIMIT,
                SearchConstants.MAX_RECENT_SEARCH_LIMIT
        );
    }

    public String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    public String resolveSort(List<String> sortBy, List<String> sortDirection, boolean hasSearchQuery) {
        String defaultSort = hasSearchQuery ? "relevance_score:desc" : "cited_by_count:desc";

        if (sortBy == null || sortBy.isEmpty()) {
            return defaultSort;
        }

        List<String> openAlexSorts = new ArrayList<>();

        for (int index = 0; index < sortBy.size(); index += 1) {
            String normalizedSortBy = sortBy.get(index) == null
                    ? ""
                    : sortBy.get(index).trim().toLowerCase(Locale.ROOT);
            String normalizedSortDirection =
                    sortDirection != null
                            && index < sortDirection.size()
                            && "asc".equalsIgnoreCase(sortDirection.get(index))
                            ? "asc"
                            : "desc";

            switch (normalizedSortBy) {
                case "citation" -> openAlexSorts.add("cited_by_count:" + normalizedSortDirection);
                case "published" -> openAlexSorts.add("publication_year:" + normalizedSortDirection);
                default -> {
                }
            }
        }

        return openAlexSorts.isEmpty() ? defaultSort : String.join(",", openAlexSorts);
    }

    public String resolveEntitySort(String sortBy, String sortDirection, boolean hasSearchQuery) {
        String defaultSort = hasSearchQuery ? "relevance_score:desc" : "works_count:desc";

        if (!StringUtils.hasText(sortBy)) {
            return defaultSort;
        }

        String normalizedSortBy = sortBy.trim().toLowerCase(Locale.ROOT);
        String normalizedSortDirection =
                "asc".equalsIgnoreCase(sortDirection) ? "asc" : "desc";

        return switch (normalizedSortBy) {
            case "works" -> "works_count:" + normalizedSortDirection;
            case "alphabetical" -> "display_name:" + normalizedSortDirection;
            default -> defaultSort;
        };
    }

    public List<String> normalizeTypeValues(List<String> values) {
        List<String> normalizedValues = normalizeStringList(values);
        List<String> result = new ArrayList<>();

        for (String value : normalizedValues) {
            result.add(extractLastSegment(value).toLowerCase(Locale.ROOT));
        }

        return result;
    }

    public List<String> normalizeSubFieldValues(List<String> values) {
        return normalizeOpenAlexIds(values);
    }

    public String normalizeEntityValue(String value) {
        return extractLastSegment(value).trim();
    }

    public List<String> normalizeEntityIds(List<String> values) {
        return normalizeOpenAlexIds(values);
    }

    public List<String> normalizeCountryValues(List<String> values) {
        List<String> normalizedValues = normalizeStringList(values);
        List<String> result = new ArrayList<>();

        for (String value : normalizedValues) {
            result.add(extractLastSegment(value).toUpperCase(Locale.ROOT));
        }

        return result;
    }

    public List<String> normalizeStringList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> uniqueValues = new LinkedHashSet<>();

        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                continue;
            }

            uniqueValues.add(value.trim());
        }

        return new ArrayList<>(uniqueValues);
    }

    public String normalizeGroupedValue(String groupBy, String rawKey) {
        if ("type".equals(groupBy)) {
            return extractLastSegment(rawKey).toLowerCase(Locale.ROOT);
        }

        if ("institutions.country_code".equals(groupBy)) {
            return extractLastSegment(rawKey).toUpperCase(Locale.ROOT);
        }

        if (
                "primary_topic.subfield.id".equals(groupBy)
                        || "primary_topic.field.id".equals(groupBy)
        ) {
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

        String trimmedValue = value.trim();
        int lastSlashIndex = trimmedValue.lastIndexOf('/');

        if (lastSlashIndex < 0 || lastSlashIndex == trimmedValue.length() - 1) {
            return trimmedValue;
        }

        return trimmedValue.substring(lastSlashIndex + 1);
    }

    private List<String> normalizeOpenAlexIds(List<String> values) {
        List<String> normalizedValues = normalizeStringList(values);
        List<String> result = new ArrayList<>();

        for (String value : normalizedValues) {
            result.add(extractLastSegment(value));
        }

        return result;
    }

    private int normalizePositiveInt(int value, int defaultValue, int maxValue) {
        if (value <= 0) {
            return defaultValue;
        }

        return Math.min(value, maxValue);
    }
}
