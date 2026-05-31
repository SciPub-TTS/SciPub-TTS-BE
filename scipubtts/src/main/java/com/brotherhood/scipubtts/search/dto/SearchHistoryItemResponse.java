package com.brotherhood.scipubtts.search.dto;

public record SearchHistoryItemResponse(
        String id,
        String query,
        String savedAt
) {
}

