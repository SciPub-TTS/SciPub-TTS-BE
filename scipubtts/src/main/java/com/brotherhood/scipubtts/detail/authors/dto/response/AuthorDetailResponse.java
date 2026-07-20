package com.brotherhood.scipubtts.detail.authors.dto.response;

import com.brotherhood.scipubtts.detail.works.dto.response.DetailWorkResponse;

import java.util.List;

public record AuthorDetailResponse(
        String entityType,
        String id,
        String displayName,
        long worksCount,
        long citedByCount,
        List<DetailWorkResponse> works,
        List<YearStat> countsByYear,
        Integer hIndex,
        Integer i10Index,
        List<String> observedInstitutions,
        List<String> observedNames,
        String orcid,
        String primaryInstitutionName,
        List<RelatedItem> topicHighlights
) {
    public record RelatedItem(
            String id,
            String displayName,
            Long count
    ) {
    }

    public record YearStat(
            int year,
            long worksCount,
            long citedByCount
    ) {
    }
}
