package com.brotherhood.scipubtts.bookmark.dto.response;

import java.util.UUID;

public record BookmarkCollectionResponse(
        UUID id,
        String name,
        long workCount
) {
}
