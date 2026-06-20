package com.brotherhood.scipubtts.search.dto.response;

public record SearchSummaryResponse(
        long totalCount,
        String entityType,
        boolean totalCountExact
) {
}
