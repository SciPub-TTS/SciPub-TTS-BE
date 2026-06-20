package com.brotherhood.scipubtts.detail.entities.dto.response;

import com.brotherhood.scipubtts.search.dto.response.SearchWorksResponse;

import java.util.List;

public record EntityDetailResponse(
        String entityType,
        String id,
        String displayName,
        String description,
        String orcid,
        String primaryInstitutionName,
        String subfieldName,
        String fieldName,
        String domainName,
        Long worksCount,
        Long citedByCount,
        Integer hIndex,
        Integer i10Index,
        List<String> observedNames,
        List<String> observedInstitutions,
        List<RelatedItem> topicHighlights,
        List<RelatedItem> siblingTopics,
        List<CountByYearItem> countsByYear,
        List<BreakdownItem> typeBreakdown,
        List<SearchWorksResponse.WorkItem> works
) {
    public record RelatedItem(
            String id,
            String displayName,
            Long count
    ) {
    }

    public record CountByYearItem(
            int year,
            long worksCount,
            long citedByCount
    ) {
    }

    public record BreakdownItem(
            String value,
            String label,
            long count
    ) {
    }
}
