package com.brotherhood.scipubtts.follow.dto.response;

import java.util.List;

public record FollowPageResponse(
        List<FollowResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
}
