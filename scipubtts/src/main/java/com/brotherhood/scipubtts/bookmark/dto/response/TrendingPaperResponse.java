package com.brotherhood.scipubtts.bookmark.dto.response;

public record TrendingPaperResponse(
        String openAlexId,
        String title,
        String authors,
        String topic,
        Integer citations,
        Long saveCount
) {
}
