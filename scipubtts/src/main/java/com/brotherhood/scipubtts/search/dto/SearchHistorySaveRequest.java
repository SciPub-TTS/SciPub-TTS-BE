package com.brotherhood.scipubtts.search.dto;

import java.util.UUID;

public class SearchHistorySaveRequest {
    private String query;
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
