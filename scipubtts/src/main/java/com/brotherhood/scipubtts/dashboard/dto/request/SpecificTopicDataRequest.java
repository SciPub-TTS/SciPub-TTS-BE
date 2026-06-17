package com.brotherhood.scipubtts.dashboard.dto.request;

public record SpecificTopicDataRequest(
        String startTime,
        String endTime,
        String fieldId
) {
}