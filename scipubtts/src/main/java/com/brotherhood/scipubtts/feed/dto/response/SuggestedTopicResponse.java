package com.brotherhood.scipubtts.feed.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record SuggestedTopicResponse(
        List<TopicData> topics
) {
    public record TopicData(
            String name,
            String topicId
    ) {}
}