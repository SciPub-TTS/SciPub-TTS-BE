package com.brotherhood.scipubtts.bookmark.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BookmarkCollectionResponse(
        UUID id,
        String name,
        long workCount,
        OffsetDateTime createdAt
) {
}
