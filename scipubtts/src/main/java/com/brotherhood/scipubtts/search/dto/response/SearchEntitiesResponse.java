package com.brotherhood.scipubtts.search.dto.response;

import java.util.List;

public record SearchEntitiesResponse(
        Meta meta,
        List<EntityItem> results
) {
    public record Meta(
            long totalCount,
            int page,
            int perPage,
            double costUsd,
            String entityType,
            String appliedFilter,
            String appliedSort,
            boolean hasMore,
            boolean totalCountExact
    ) {
    }

    public record EntityItem(
            String id,
            String entityType,
            String displayName,
            String primaryInstitutionName,
            String primaryTopicName,
            String subfieldName,
            String fieldName,
            String domainName,
            long worksCount
    ) {
    }
}
