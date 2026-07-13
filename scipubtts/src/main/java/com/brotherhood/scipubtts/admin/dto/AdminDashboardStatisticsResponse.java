package com.brotherhood.scipubtts.admin.dto;

import java.time.OffsetDateTime;

public record AdminDashboardStatisticsResponse(
        StatisticCard<Long> totalUsers,
        StatisticCard<Long> activeTrends,
        StatisticCard<Long> bannedUsers,
        StatisticCard<Long> apiCallsUsed,
        StatisticCard<Long> apiCallsToday,
        StatisticCard<Long> totalApiCredit,
        StatisticCard<Long> totalSubfields,
        StatisticCard<Long> totalTopics,
        StatisticCard<OffsetDateTime> lastSynchronization
) {
    public record StatisticCard<T>(
            T value,
            String description,
            String delta
    ) {
    }
}
