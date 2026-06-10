package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.search.dto.SearchWorksQueryRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class SearchFilterBuilder {

    // This class converts our request DTO into one OpenAlex filter string.
    private final SearchQuerySupport searchQuerySupport;

    public SearchFilterBuilder(SearchQuerySupport searchQuerySupport) {
        this.searchQuerySupport = searchQuerySupport;
    }

    public String build(SearchWorksQueryRequest request) {
        List<String> filterParts = new ArrayList<>();

        // Add each filter group one by one so the final string is easy to reason about.
        addYearFilter(request, filterParts);
        addOrFilter(filterParts, "type", searchQuerySupport.normalizeTypeValues(request.getType()));
        addBooleanFilter(filterParts, "is_oa", request.getOpenAccess());
        addOrFilter(filterParts, "primary_topic.subfield.id", searchQuerySupport.normalizeSubFieldValues(request.getSubField()));
        addOrFilter(filterParts, "authorships.author.id", searchQuerySupport.normalizeEntityIds(request.getAuthor()));
        addOrFilter(filterParts, "authorships.institutions.id", searchQuerySupport.normalizeEntityIds(request.getInstitution()));
        addBooleanFilter(filterParts, "has_content.pdf", request.getPdf());
        addOrFilter(filterParts, "institutions.country_code", searchQuerySupport.normalizeCountryValues(request.getCountry()));
        addCitationFilter(request, filterParts);
        addOrFilter(filterParts, "primary_location.source.id", searchQuerySupport.normalizeEntityIds(request.getSource()));
        addOrFilter(filterParts, "awards.id", searchQuerySupport.normalizeEntityIds(request.getAward()));
        addOrcidFilter(request.getIndexedByOrcid(), filterParts);

        return String.join(",", filterParts);
    }

    private void addYearFilter(SearchWorksQueryRequest request, List<String> filterParts) {
        // Year can work in exact mode or range mode.
        String yearMode = searchQuerySupport.normalizeMode(request.getYearMode());

        if ("exact".equals(yearMode) && request.getYearExact() != null) {
            filterParts.add("publication_year:" + request.getYearExact());
            return;
        }

        Integer yearFrom = request.getYearFrom();
        Integer yearTo = request.getYearTo();

        if (yearFrom != null && yearTo != null) {
            filterParts.add("publication_year:" + yearFrom + "-" + yearTo);
            return;
        }

        if (yearFrom != null) {
            filterParts.add("publication_year:>" + yearFrom);
        }

        if (yearTo != null) {
            filterParts.add("publication_year:<" + yearTo);
        }
    }

    private void addCitationFilter(SearchWorksQueryRequest request, List<String> filterParts) {
        // Citation count also supports exact mode or range mode.
        String citationMode = searchQuerySupport.normalizeMode(request.getCitationMode());

        if ("exact".equals(citationMode) && request.getCitationExact() != null) {
            filterParts.add("cited_by_count:" + request.getCitationExact());
            return;
        }

        Integer citationMin = request.getCitationMin();
        Integer citationMax = request.getCitationMax();

        if (citationMin != null && citationMax != null) {
            filterParts.add("cited_by_count:" + citationMin + "-" + citationMax);
            return;
        }

        if (citationMin != null) {
            filterParts.add("cited_by_count:>" + citationMin);
        }

        if (citationMax != null) {
            filterParts.add("cited_by_count:<" + citationMax);
        }
    }

    private void addBooleanFilter(List<String> filterParts, String field, Boolean value) {
        // Only add the boolean filter when the client actually sent one.
        if (value == null) {
            return;
        }

        filterParts.add(field + ":" + value);
    }

    private void addOrcidFilter(String indexedByOrcid, List<String> filterParts) {
        // Frontend sends "is" or "is not"; OpenAlex expects true or false.
        if (!StringUtils.hasText(indexedByOrcid)) {
            return;
        }

        String normalized = indexedByOrcid.trim().toLowerCase(Locale.ROOT);

        if ("is".equals(normalized)) {
            filterParts.add("has_orcid:true");
            return;
        }

        if ("is not".equals(normalized)) {
            filterParts.add("has_orcid:false");
        }
    }

    private void addOrFilter(List<String> filterParts, String field, List<String> values) {
        // OpenAlex OR syntax is value1|value2|value3.
        if (values.isEmpty()) {
            return;
        }

        List<String> limitedValues = values;
        if (values.size() > SearchConstants.FILTER_OPTION_LIMIT) {
            limitedValues = values.subList(0, SearchConstants.FILTER_OPTION_LIMIT);
        }

        filterParts.add(field + ":" + String.join("|", limitedValues));
    }
}
