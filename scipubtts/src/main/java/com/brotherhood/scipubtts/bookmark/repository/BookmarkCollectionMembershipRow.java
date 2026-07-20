package com.brotherhood.scipubtts.bookmark.repository;

import java.util.UUID;

public record BookmarkCollectionMembershipRow(
        UUID bookmarkId,
        UUID collectionId,
        String collectionName,
        long workCount
) {
}
