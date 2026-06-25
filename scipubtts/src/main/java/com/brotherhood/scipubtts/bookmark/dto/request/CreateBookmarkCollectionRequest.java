package com.brotherhood.scipubtts.bookmark.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBookmarkCollectionRequest(
        @NotBlank(message = "Collection name is required")
        @Size(max = 120, message = "Collection name must be 120 characters or fewer")
        String name
) {
}
