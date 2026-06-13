package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.search.dto.SearchWorksQueryRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class SearchFilterBuilder {

    private final SearchQuerySupport searchQuerySupport;

    public SearchFilterBuilder(SearchQuerySupport searchQuerySupport) {
        this.searchQuerySupport = searchQuerySupport;
    }

    public String build(SearchWorksQueryRequest request) {
        List<String> filterParts = new ArrayList<>();
        filterParts.add(SearchConstants.WORKS_SCOPE_FILTER);

        addYearFilter(request, filterParts);
        addListFilter(filterParts, "type", searchQuerySupport.normalizeTypeValues(request.getType()));
        addBooleanFilter(filterParts, "is_oa", request.getOpenAccess());
        addListFilter(
                filterParts,
                "primary_topic.subfield.id",
                searchQuerySupport.normalizeSubFieldValues(request.getSubField())
        );
        addListFilter(filterParts, "authorships.author.id", searchQuerySupport.normalizeEntityIds(request.getAuthor()));
        addListFilter(
                filterParts,
                "authorships.institutions.id",
                searchQuerySupport.normalizeEntityIds(request.getInstitution())
        );
        addBooleanFilter(filterParts, "has_content.pdf", request.getPdf());
        addListFilter(
                filterParts,
                "institutions.country_code",
                searchQuerySupport.normalizeCountryValues(request.getCountry())
        );
        addCitationFilter(request, filterParts);
        addListFilter(
                filterParts,
                "primary_location.source.id",
                searchQuerySupport.normalizeEntityIds(request.getSource())
        );
        addListFilter(filterParts, "awards.id", searchQuerySupport.normalizeEntityIds(request.getAward()));
        addOrcidFilter(filterParts, request.getIndexedByOrcid());

        return String.join(",", filterParts);
    }

    private void addYearFilter(SearchWorksQueryRequest request, List<String> filterParts) {
        addRangeFilter(
                filterParts,
                "publication_year",
                request.getYearExact(),
                request.getYearFrom(),
                request.getYearTo()
        );
    }

    private void addCitationFilter(SearchWorksQueryRequest request, List<String> filterParts) {
        addRangeFilter(
                filterParts,
                "cited_by_count",
                request.getCitationExact(),
                request.getCitationMin(),
                request.getCitationMax()
        );
    }

    private void addRangeFilter(
            List<String> filterParts,
            String field,
            Integer exactValue,
            Integer minValue,
            Integer maxValue
    ) {
        if (exactValue != null) {
            filterParts.add(field + ":" + exactValue);
            return;
        }

        if (minValue != null && maxValue != null) {
            filterParts.add(field + ":" + minValue + "-" + maxValue);
            return;
        }

        if (minValue != null) {
            filterParts.add(field + ":>" + minValue);
        }

        if (maxValue != null) {
            filterParts.add(field + ":<" + maxValue);
        }
    }

    private void addBooleanFilter(List<String> filterParts, String field, Boolean value) {
        if (value != null) {
            filterParts.add(field + ":" + value);
        }
    }

    private void addOrcidFilter(List<String> filterParts, String indexedByOrcid) {
        if (!StringUtils.hasText(indexedByOrcid)) {
            return;
        }

        String normalizedValue = indexedByOrcid.trim().toLowerCase(Locale.ROOT);

        if ("is".equals(normalizedValue)) {
            filterParts.add("has_orcid:true");
        } else if ("is not".equals(normalizedValue)) {
            filterParts.add("has_orcid:false");
        }
    }

    private void addListFilter(List<String> filterParts, String field, List<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }

        List<String> limitedValues = values;
        if (values.size() > SearchConstants.FILTER_OPTION_LIMIT) {
            limitedValues = values.subList(0, SearchConstants.FILTER_OPTION_LIMIT);
        }

        filterParts.add(field + ":" + String.join("|", limitedValues));
    }
}
