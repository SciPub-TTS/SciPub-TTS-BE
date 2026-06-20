package com.brotherhood.scipubtts.socialhub.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SocialPostDetailResponse(
        UUID id,
        String title,
        String body,
        int likeCount,
        boolean liked,
        AuthorInfo author,
        List<ReferenceInfo> references,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,

        // true nếu request update vừa làm reset like_count về 0
        // (do thay đổi reference) — FE dùng để show toast cảnh báo
        boolean likesReset
) {
    public record AuthorInfo(UUID id, String fullName) {}

    public record ReferenceInfo(
            UUID id,
            String openalexId,
            String titleSnapshot,
            String authorsSnapshot,
            String sourceSnapshot,
            Integer yearSnapshot,
            String doiSnapshot
    ) {}
}