package com.brotherhood.scipubtts.admin.dto;

public record AdminUserActivityResponse(
        long topicCount,
        long authorCount,
        long bookmarkCount,
        long searchCount
) {
}
