package com.brotherhood.scipubtts.feed.dto.response;

import lombok.Builder;

@Builder
public record SuggestedTopicResponse(
                                Long id,
                                String topicName,
                                Double trendScore) {
}
