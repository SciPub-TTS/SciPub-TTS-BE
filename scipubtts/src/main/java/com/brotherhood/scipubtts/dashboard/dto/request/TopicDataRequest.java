package com.brotherhood.scipubtts.dashboard.dto.request;

public record TopicDataRequest(
        String startTime,
        String endTime,
        String fieldId,
        String formula
) {
}