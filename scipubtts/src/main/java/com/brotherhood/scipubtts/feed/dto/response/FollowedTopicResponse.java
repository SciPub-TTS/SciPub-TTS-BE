package com.brotherhood.scipubtts.feed.dto.response;

import lombok.Builder;

@Builder
public record FollowedTopicResponse(
                String name,
                String status) {
}
