package com.brotherhood.scipubtts.socialhub.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SocialPostDetailResponse(
        UUID id,
        String title,
        String body,
        List<String> topicTag,
        int likeCount,
        boolean liked,
        AuthorInfo author,
        List<ReferenceInfo> references,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        boolean likesReset
) {
    public record AuthorInfo(UUID id, String fullName) {}

    public record ReferenceInfo(
            UUID id,
            String openalexId,
            String titleSnapshot,
            String authorsSnapshot,
            Integer yearSnapshot
    ) {}
}