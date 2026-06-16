package com.brotherhood.scipubtts.admin.dto;

public record AdminUserBanSummaryResponse(
        long active,
        long banned,
        long total,
        int activePercentage,
        int bannedPercentage
) {
}
