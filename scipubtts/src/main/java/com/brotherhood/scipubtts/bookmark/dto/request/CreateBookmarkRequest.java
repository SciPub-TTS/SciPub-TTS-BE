package com.brotherhood.scipubtts.bookmark.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateBookmarkRequest(
        @NotBlank(message = "OpenAlex ID is required")
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
