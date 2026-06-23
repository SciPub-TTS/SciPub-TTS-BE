package com.brotherhood.scipubtts.socialhub.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SocialPostSummaryResponse(
        UUID id,
        String title,
        String bodyPreview,
        List<String> topicTag,
        List<ReferenceInfo> references,
        int likeCount,
        boolean liked,
        AuthorInfo author,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public record AuthorInfo(UUID id, String fullName) {}

    public record ReferenceInfo(
            UUID id,
            String openalexId,
            String titleSnapshot,
            String authorsSnapshot,
            List<String> authorOpenAlexIdsSnapshot,
            String topicSnapshot,
            String topicOpenAlexIdSnapshot,
            Integer yearSnapshot
    ) {}
}
