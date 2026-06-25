package com.brotherhood.scipubtts.admin.dto;

import java.util.List;

public record AdminUserSearchHistoryPageResponse(
        List<AdminUserSearchHistoryItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
}
