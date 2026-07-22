package com.brotherhood.scipubtts.socialhub.dto.response;

import java.util.List;

public record SocialPostPageResponse(
        List<SocialPostSummaryResponse> content,
        long totalElements
) {
}
