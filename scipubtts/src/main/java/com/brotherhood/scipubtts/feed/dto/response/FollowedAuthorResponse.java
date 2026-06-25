package com.brotherhood.scipubtts.feed.dto.response;

import lombok.Builder;

@Builder
public record FollowedAuthorResponse(
        String id, // OpenAlex Target Identifier (e.g. A1001)
        String name,
        String field
) {}
