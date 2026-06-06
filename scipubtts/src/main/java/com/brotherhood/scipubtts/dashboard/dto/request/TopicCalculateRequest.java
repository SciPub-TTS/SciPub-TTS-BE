package com.brotherhood.scipubtts.dashboard.dto.request;

public record TopicCalculateRequest(
        String startTime,
        String endTime,
        String fieldId
) {
}