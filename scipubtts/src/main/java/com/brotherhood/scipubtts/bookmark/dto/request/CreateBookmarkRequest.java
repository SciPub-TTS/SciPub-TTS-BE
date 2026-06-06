package com.brotherhood.scipubtts.bookmark.dto.request;

public record CreateBookmarkRequest(
        String openAlexId,
        String titleSnapshot,
        String authorsSnapshot,
        String sourceSnapshot,
        String topicSnapshot,
        Integer publicationYear,
        Integer citationSnapshot,
        String note
) {
}
