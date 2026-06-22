package com.brotherhood.scipubtts.search.dto;

public record HotTopicItemResponse(
        String topicId,
        String name,
        Integer fieldId,
        Long works,
        Long citations
) {
}
