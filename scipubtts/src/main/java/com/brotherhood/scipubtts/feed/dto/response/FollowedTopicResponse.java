package com.brotherhood.scipubtts.feed.dto.response;

import lombok.Builder;

@Builder
public record FollowedTopicResponse(
        String id, // OpenAlex Target Identifier (e.g. T1001)
        String name,
        String status
) {}
