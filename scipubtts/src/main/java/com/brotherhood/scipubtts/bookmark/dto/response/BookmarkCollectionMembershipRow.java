package com.brotherhood.scipubtts.bookmark.dto.response;

import java.util.UUID;

public record BookmarkCollectionMembershipRow(
        UUID bookmarkId,
        UUID collectionId,
        String collectionName
) {
}
