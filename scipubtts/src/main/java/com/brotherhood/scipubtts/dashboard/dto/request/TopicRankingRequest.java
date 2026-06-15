package com.brotherhood.scipubtts.dashboard.dto.request;

public record TopicRankingRequest(
        String startTime,
        String endTime,
        String fieldId,
        String formula
) {
}