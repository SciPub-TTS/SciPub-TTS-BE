package com.brotherhood.scipubtts.dashboard.dto.request.openalex;

public record OpenAlexTopicFilterRequest(
        String startTime,
        String endTime,
        String topicId
) {
}