package com.brotherhood.scipubtts.socialhub.dto.response;

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
        String createdAt,
        String updatedAt,
        boolean likesReset
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
