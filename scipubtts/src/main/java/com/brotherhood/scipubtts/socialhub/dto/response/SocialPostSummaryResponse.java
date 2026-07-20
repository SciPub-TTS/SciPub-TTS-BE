package com.brotherhood.scipubtts.socialhub.dto.response;

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
        String createdAt,
        String updatedAt
) {

    public record AuthorInfo(UUID id, String fullName, String avatarUrl) {}

    public record ReferenceInfo(
            UUID id,
            String openalexId,
            String titleSnapshot,
            String authorsSnapshot,
            List<String> authorOpenAlexIdsSnapshot,
            String workTypeSnapshot,
            String topicSnapshot,
            String topicOpenAlexIdSnapshot,
            Integer yearSnapshot
    ) {}
}
