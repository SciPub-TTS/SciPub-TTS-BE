package com.brotherhood.scipubtts.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(
        name = "SearchHistorySaveRequest",
        description = "Request body used to save one search keyword into the user's search history."
)
public record SearchHistorySaveRequest(
        @Schema(
                description = "Search keyword to save.",
                example = "AI in education"
        )
        String query,
        @Schema(hidden = true)
        UUID userId
) {
    public SearchHistorySaveRequest withUserId(UUID userId) {
        return new SearchHistorySaveRequest(query, userId);
    }
}
