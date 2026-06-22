package com.brotherhood.scipubtts.user.dto.response;


public record DashboardResponse (
    long followTopics,
    long followAuthors,
    long bookmarkMarked
) {
}
