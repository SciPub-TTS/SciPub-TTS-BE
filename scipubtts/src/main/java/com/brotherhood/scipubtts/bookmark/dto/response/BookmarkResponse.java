package com.brotherhood.scipubtts.bookmark.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record BookmarkResponse(
        UUID id,
        String openAlexId,
        String title,
        String authors,
        String workType,
        String source,
        String topic,
        Integer publicationYear,
        Integer citationCount,
        List<BookmarkCollectionSummaryResponse> collections,
        OffsetDateTime createdAt
) {
}
