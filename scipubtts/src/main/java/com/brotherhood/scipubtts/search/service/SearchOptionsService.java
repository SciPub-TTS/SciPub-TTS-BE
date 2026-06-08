package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.search.dto.SearchFilterOptionsResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchOptionsService {

    private final OpenAlexClient openAlexClient;
    private final SearchQuerySupport searchQuerySupport;
    private final OpenAlexMapReader openAlexMapReader;

    public SearchOptionsService(
            OpenAlexClient openAlexClient,
            SearchQuerySupport searchQuerySupport,
            OpenAlexMapReader openAlexMapReader
    ) {
        this.openAlexClient = openAlexClient;
        this.searchQuerySupport = searchQuerySupport;
        this.openAlexMapReader = openAlexMapReader;
    }

    public SearchFilterOptionsResponse getFilterOptions(String keyword, int limit, int page) {
        int normalizedLimit = searchQuerySupport.normalizeFilterOptionLimit(limit);
        int normalizedPage = searchQuerySupport.normalizeOptionPage(page);
        String normalizedKeyword = searchQuerySupport.normalizeKeyword(keyword);

        List<SearchFilterOptionsResponse.FacetOption> typeOptions =
                fetchGroupedWorkOptions("type", normalizedLimit, normalizedPage);
        List<SearchFilterOptionsResponse.FacetOption> subFieldOptions =
                fetchGroupedWorkOptions("primary_topic.subfield.id", normalizedLimit, normalizedPage);
        List<SearchFilterOptionsResponse.FacetOption> countryOptions =
                StringUtils.hasText(normalizedKeyword)
                        ? fetchCountryOptions(normalizedKeyword, normalizedLimit, normalizedPage)
                        : fetchGroupedWorkOptions("institutions.country_code", normalizedLimit, normalizedPage);

        List<SearchFilterOptionsResponse.EntityOption> sourceOptions = fetchEntityOptions(
                "/sources",
                normalizedKeyword,
                normalizedLimit,
                normalizedPage,
                "id,display_name,works_count",
                "works_count",
                "works_count:desc"
        );

        List<SearchFilterOptionsResponse.EntityOption> authorOptions = fetchEntityOptions(
                "/authors",
                normalizedKeyword,
                normalizedLimit,
                normalizedPage,
                "id,display_name,works_count",
                "works_count",
                "works_count:desc"
        );

        List<SearchFilterOptionsResponse.EntityOption> institutionOptions = fetchEntityOptions(
                "/institutions",
                normalizedKeyword,
                normalizedLimit,
                normalizedPage,
                "id,display_name,works_count,country_code",
                "works_count",
                "works_count:desc"
        );

        List<SearchFilterOptionsResponse.EntityOption> awardOptions = fetchEntityOptions(
                "/awards",
                normalizedKeyword,
                normalizedLimit,
                normalizedPage,
                "id,display_name",
                null,
                null
        );

        int currentYear = Year.now().getValue();
        long totalWorks = fetchTotalWorksCount();
        int maxCitation = fetchMaximumCitationCount();

        return new SearchFilterOptionsResponse(
                totalWorks,
                new SearchFilterOptionsResponse.YearRange(SearchConstants.MIN_YEAR, currentYear),
                typeOptions,
                new SearchFilterOptionsResponse.ToggleFilter("is_oa", false),
                subFieldOptions,
                authorOptions,
                institutionOptions,
                new SearchFilterOptionsResponse.ToggleFilter("has_content.pdf", false),
                new SearchFilterOptionsResponse.CitationRange(SearchConstants.MIN_CITATION, maxCitation),
                countryOptions,
                sourceOptions,
                awardOptions,
                new SearchFilterOptionsResponse.EnumFilter("has_orcid", List.of("is", "is not"), "")
        );
    }

    private List<SearchFilterOptionsResponse.FacetOption> fetchGroupedWorkOptions(String groupBy, int limit, int page) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("group_by", groupBy);
        queryParams.put("per_page", String.valueOf(limit));
        queryParams.put("page", String.valueOf(page));
        queryParams.put("sort", "count:desc");

        Map<String, Object> response = openAlexClient.get("/works", queryParams);
        List<Map<String, Object>> groups = openAlexMapReader.getMapList(response, "group_by");
        List<SearchFilterOptionsResponse.FacetOption> options = new ArrayList<>();

        for (Map<String, Object> group : groups) {
            String key = openAlexMapReader.getString(group, "key");
            String label = openAlexMapReader.sanitizeDisplayText(openAlexMapReader.getString(group, "key_display_name"));
            long count = openAlexMapReader.getLong(group, "count", 0L);

            if (key.isBlank() || label.isBlank()) {
                continue;
            }

            options.add(new SearchFilterOptionsResponse.FacetOption(
                    searchQuerySupport.normalizeGroupedValue(groupBy, key),
                    label,
                    count
            ));
        }

        return options;
    }

    private List<SearchFilterOptionsResponse.EntityOption> fetchEntityOptions(
            String path,
            String keyword,
            int limit,
            int page,
            String selectFields,
            String countField,
            String defaultSort
    ) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("per_page", String.valueOf(limit));
        queryParams.put("page", String.valueOf(page));
        queryParams.put("select", selectFields);

        if (StringUtils.hasText(keyword)) {
            queryParams.put("search", keyword);
        } else if (StringUtils.hasText(defaultSort)) {
            queryParams.put("sort", defaultSort);
        }

        Map<String, Object> response = openAlexClient.get(path, queryParams);
        List<Map<String, Object>> results = openAlexMapReader.getMapList(response, "results");
        List<SearchFilterOptionsResponse.EntityOption> options = new ArrayList<>();

        for (Map<String, Object> result : results) {
            String id = openAlexMapReader.getString(result, "id");
            String label = openAlexMapReader.sanitizeDisplayText(openAlexMapReader.getString(result, "display_name"));

            if (id.isBlank() || label.isBlank()) {
                continue;
            }

            Long count = null;
            if (countField != null && result.get(countField) != null) {
                count = openAlexMapReader.getLong(result, countField, 0L);
            }

            options.add(new SearchFilterOptionsResponse.EntityOption(id, label, count));
        }

        return options;
    }

    private List<SearchFilterOptionsResponse.FacetOption> fetchCountryOptions(String keyword, int limit, int page) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("per_page", String.valueOf(limit));
        queryParams.put("page", String.valueOf(page));
        queryParams.put("select", "id,display_name,works_count");

        if (StringUtils.hasText(keyword)) {
            queryParams.put("search", keyword);
        }

        Map<String, Object> response = openAlexClient.get("/countries", queryParams);
        List<Map<String, Object>> results = openAlexMapReader.getMapList(response, "results");
        List<SearchFilterOptionsResponse.FacetOption> options = new ArrayList<>();

        for (Map<String, Object> result : results) {
            String id = openAlexMapReader.getString(result, "id");
            String label = openAlexMapReader.sanitizeDisplayText(openAlexMapReader.getString(result, "display_name"));
            long count = openAlexMapReader.getLong(result, "works_count", 0L);
            String value = searchQuerySupport.normalizeCountryOptionValue(id);

            if (value.isBlank() || label.isBlank()) {
                continue;
            }

            options.add(new SearchFilterOptionsResponse.FacetOption(value, label, count));
        }

        return options;
    }

    private long fetchTotalWorksCount() {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("per_page", "1");
        queryParams.put("select", "id");

        Map<String, Object> response = openAlexClient.get("/works", queryParams);
        Map<String, Object> meta = openAlexMapReader.getMap(response, "meta");

        return openAlexMapReader.getLong(meta, "count", 0L);
    }

    private int fetchMaximumCitationCount() {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("per_page", "1");
        queryParams.put("sort", "cited_by_count:desc");
        queryParams.put("select", "cited_by_count");

        Map<String, Object> response = openAlexClient.get("/works", queryParams);
        List<Map<String, Object>> results = openAlexMapReader.getMapList(response, "results");

        if (results.isEmpty()) {
            return 0;
        }

        int maxCitationCount = openAlexMapReader.getInt(results.get(0), "cited_by_count", 0);
        return Math.max(maxCitationCount, 0);
    }
}
