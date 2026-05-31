package com.brotherhood.scipubtts.search.dto;

import java.util.List;

public record SearchWorksResponse(
        Meta meta,
        List<WorkItem> results
) {
    public record Meta(
            long totalCount,
            int page,
            int perPage,
            long dbResponseTimeMs,
            double costUsd,
            String appliedFilter,
            String appliedSort
    ) {
    }

    public record WorkItem(
            String id,
            String title,
            String abstractText,
            String doi,
            Integer publicationYear,
            Integer citedByCount,
            Boolean openAccess,
            Boolean hasPdf,
            String pdfUrl,
            Boolean hasOrcid,
            String type,
            String topicName,
            String subFieldName,
            String sourceId,
            String sourceName,
            List<String> authors
    ) {
    }
}

