package com.brotherhood.scipubtts.feed.dto.response;

import lombok.Builder;

@Builder
public record FeedAuthorResponse(
        String name,
        Boolean following
) {}
