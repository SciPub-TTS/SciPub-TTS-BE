package com.brotherhood.scipubtts.socialhub.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SocialPostSummaryResponse(
        UUID id,
        String title,
        String bodyPreview,     // 200 ký tự đầu
        int likeCount,
        boolean liked,          // hybrid-view: true nếu user đã like
        AuthorInfo author,
        OffsetDateTime createdAt
) {

    public record AuthorInfo(UUID id, String fullName) {}
}