package com.brotherhood.scipubtts.dashboard.dto.request;

public record TopicCalculateAllRequest(
        String startTime,
        String endTime,
        String fieldId
) {
}