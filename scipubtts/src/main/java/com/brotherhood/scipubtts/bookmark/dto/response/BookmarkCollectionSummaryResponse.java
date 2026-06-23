package com.brotherhood.scipubtts.bookmark.dto.response;

import java.util.UUID;

public record BookmarkCollectionSummaryResponse(
        UUID id,
        String name
) {
}
