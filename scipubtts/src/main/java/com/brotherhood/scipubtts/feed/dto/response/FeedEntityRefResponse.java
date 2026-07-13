package com.brotherhood.scipubtts.feed.dto.response;

import lombok.Builder;

@Builder
public record FeedEntityRefResponse(
        String id,
        String displayName
) {}
