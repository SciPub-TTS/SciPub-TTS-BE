package com.brotherhood.scipubtts.bookmark.dto.response;

import java.util.List;
import java.util.UUID;

public record BookmarkStatusResponse(
        boolean bookmarked,
        UUID bookmarkId,
        String openAlexId,
        List<BookmarkCollectionSummaryResponse> collections
) {
}
