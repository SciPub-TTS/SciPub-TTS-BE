package com.brotherhood.scipubtts.feed.dto.response;

import lombok.Builder;

@Builder
public record FollowSummaryResponse(
                Long id,
                String name) {
}
