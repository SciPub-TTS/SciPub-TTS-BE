package com.brotherhood.scipubtts.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "SearchHistorySaveRequest")
public record SearchHistorySaveRequest(
        @Schema(example = "AI in education")
        String query,
        @Schema(hidden = true)
        UUID userId
) {
    public SearchHistorySaveRequest withUserId(UUID userId) {
        return new SearchHistorySaveRequest(query, userId);
    }
}
