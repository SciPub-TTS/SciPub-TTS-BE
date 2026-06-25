package com.brotherhood.scipubtts.bookmark.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UpdateBookmarkCollectionItemsRequest(
        @NotEmpty(message = "At least one bookmark is required")
        List<@NotNull(message = "Bookmark id is required") UUID> bookmarkIds
) {
}
