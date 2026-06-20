package com.brotherhood.scipubtts.search.dto.response;

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

    public record EntityRef(
            String id,
            String displayName
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
            String topic,
            String subFieldName,
            String sourceId,
            String sourceName,
            List<String> authors,
            List<EntityRef> authorRefs,
            List<String> keywords,
            EntityRef topicRef,
            Boolean matchesTrendingKeyword,
            Boolean matchesTrendingTopic,
            Double trendingScore
    ) {
    }
}
