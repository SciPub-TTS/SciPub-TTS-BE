package com.brotherhood.scipubtts.journalDaily.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record JournalDailyArticleResponse(
        Long id,
        String externalId,
        String title,
        String summary,
        String author,
        String thumbnailUrl,
        String sourceUrl,
        OffsetDateTime publishedAt,
        String category,
        List<String> tags
) {
}