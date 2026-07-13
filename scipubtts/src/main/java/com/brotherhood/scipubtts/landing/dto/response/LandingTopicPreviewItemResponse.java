package com.brotherhood.scipubtts.landing.dto.response;

public record LandingTopicPreviewItemResponse(
        String topicId,
        String name,
        Integer fieldId,
        Long works,
        Long citations
) {
}
