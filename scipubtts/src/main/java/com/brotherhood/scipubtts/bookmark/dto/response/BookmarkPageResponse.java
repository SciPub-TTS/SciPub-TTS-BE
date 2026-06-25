package com.brotherhood.scipubtts.bookmark.dto.response;

import java.util.List;

public record BookmarkPageResponse(
        List<BookmarkResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
}
