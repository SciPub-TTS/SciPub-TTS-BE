package com.brotherhood.scipubtts.bookmark.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateBookmarkRequest(
        @NotBlank(message = "OpenAlex ID is required")
        String openAlexId,
        String titleSnapshot,
        String authorsSnapshot,
        List<String> authorOpenAlexIdsSnapshot,
        String sourceSnapshot,
        String topicSnapshot,
        String topicOpenAlexIdSnapshot,
        Integer publicationYear,
        Integer citationSnapshot,
        String note
) {
}
