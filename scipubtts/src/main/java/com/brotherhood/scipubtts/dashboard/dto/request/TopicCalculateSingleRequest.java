package com.brotherhood.scipubtts.dashboard.dto.request;

public record TopicCalculateSingleRequest(
        String startTime,
        String endTime,
        String topicId
) {
}