package com.brotherhood.scipubtts.feed.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record FeedResponse(
                List<FeedItemResponse> items,
                long totalItems) {
}
