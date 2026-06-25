package com.brotherhood.scipubtts.feed.dto.response;

import lombok.Builder;

@Builder
public record SuggestedTopicResponse(
        String id, // OpenAlex Target Identifier
        String name
) {}
