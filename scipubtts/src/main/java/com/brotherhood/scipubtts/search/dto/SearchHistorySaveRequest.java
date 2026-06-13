package com.brotherhood.scipubtts.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(
        name = "SearchHistorySaveRequest",
        description = "Request body used to save one search keyword into the user's search history."
)
public class SearchHistorySaveRequest {
    @Schema(
            description = "Search keyword to save.",
            example = "AI in education"
    )
    private String query;

    @Schema(hidden = true)
    private UUID userId;

    public SearchHistorySaveRequest withUserId(UUID userId) {
        SearchHistorySaveRequest request = new SearchHistorySaveRequest();
        request.setQuery(this.query);
        request.setUserId(userId);
        return request;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
