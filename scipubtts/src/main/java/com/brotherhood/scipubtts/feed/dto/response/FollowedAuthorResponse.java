package com.brotherhood.scipubtts.feed.dto.response;

import lombok.Builder;

@Builder
public record FollowedAuthorResponse(
        String name,
        String field
) {}
