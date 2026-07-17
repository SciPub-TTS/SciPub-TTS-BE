package com.brotherhood.scipubtts.admin.dto;

import java.util.List;

public record AdminApiCallLogPageResponse(
        List<AdminApiCallLogItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
}
