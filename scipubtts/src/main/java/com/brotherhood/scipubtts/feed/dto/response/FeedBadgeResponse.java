package com.brotherhood.scipubtts.feed.dto.response;

import lombok.Builder;

@Builder
public record FeedBadgeResponse(
    String label,
    String tone // Maps to: "author" | "match" | "rising" | "stable" | "topic"
) {
}
