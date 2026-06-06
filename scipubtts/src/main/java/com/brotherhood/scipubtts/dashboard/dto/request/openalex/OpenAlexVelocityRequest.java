package com.brotherhood.scipubtts.dashboard.dto.request.openalex;

public record OpenAlexVelocityRequest(
        String startTime,
        String endTime,
        String topicId
) {
}