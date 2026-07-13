package com.brotherhood.scipubtts.admin.dto;

import java.time.OffsetDateTime;

public record AdminUserSearchHistoryItemResponse(
        String keyword,
        OffsetDateTime searchedAt
) {
}
