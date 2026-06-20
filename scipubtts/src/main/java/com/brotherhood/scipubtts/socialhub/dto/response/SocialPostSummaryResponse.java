package com.brotherhood.scipubtts.socialhub.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SocialPostSummaryResponse(
        UUID id,
        String title,
        String bodyPreview,
        List<String> topicTag,
        int likeCount,
        boolean liked,
        AuthorInfo author,
        OffsetDateTime createdAt
) {

    public record AuthorInfo(UUID id, String fullName) {}
}