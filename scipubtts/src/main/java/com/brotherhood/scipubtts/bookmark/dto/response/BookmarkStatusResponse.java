package com.brotherhood.scipubtts.bookmark.dto.response;

import java.util.UUID;

public record BookmarkStatusResponse (
        boolean bookmarked,
        UUID bookmarkId,
        String openAlexid
) {
}
