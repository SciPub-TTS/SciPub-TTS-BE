package com.brotherhood.scipubtts.detail.topics.dto.response;

import com.brotherhood.scipubtts.detail.works.dto.response.DetailWorkResponse;

import java.util.List;

public record TopicDetailResponse(
        String entityType,
        String id,
        String displayName,
        long worksCount,
        long citedByCount,
        List<DetailWorkResponse> works,
        List<YearStat> countsByYear,
        String description,
        String domainName,
        String fieldName,
        List<RelatedItem> siblingTopics,
        String subfieldName,
        List<TypeBreakdownItem> typeBreakdown
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

    public record TypeBreakdownItem(
            String value,
            String label,
            long count
    ) {
    }
}
