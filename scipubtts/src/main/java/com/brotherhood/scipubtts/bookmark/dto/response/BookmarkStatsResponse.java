package com.brotherhood.scipubtts.bookmark.dto.response;

public record BookmarkStatsResponse(
        int totalPapers,
        int totalTopics,
        int totalSources,
        int totalAuthors
) {
}
