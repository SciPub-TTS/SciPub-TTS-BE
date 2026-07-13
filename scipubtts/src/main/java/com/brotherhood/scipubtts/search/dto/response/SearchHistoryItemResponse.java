package com.brotherhood.scipubtts.search.dto.response;

public record SearchHistoryItemResponse(
        String id,
        String query,
        String savedAt
) {
}
