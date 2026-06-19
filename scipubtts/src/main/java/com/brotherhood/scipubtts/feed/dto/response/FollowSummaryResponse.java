package com.brotherhood.scipubtts.feed.dto.response;

import java.util.UUID;

import lombok.Builder;

@Builder
public record FollowSummaryResponse(
                Long id,
                String name) {
}
