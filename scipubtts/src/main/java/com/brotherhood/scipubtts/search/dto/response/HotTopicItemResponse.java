package com.brotherhood.scipubtts.search.dto.response;

public record HotTopicItemResponse(
        String topicId,
        String name,
        Integer fieldId,
        Long works,
        Long citations
) {
}
