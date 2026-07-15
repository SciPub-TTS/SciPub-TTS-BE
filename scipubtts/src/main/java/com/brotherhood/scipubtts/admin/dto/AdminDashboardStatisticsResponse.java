package com.brotherhood.scipubtts.admin.dto;

public record AdminDashboardStatisticsResponse(
        StatisticCard<Long> totalUsers,
        StatisticCard<Long> bannedUser,
        StatisticCard<Long> totalSubfields,
        StatisticCard<Long> totalTopics,
        StatisticCard<Long> totalTopicTrend,
        StatisticCard<Long> totalKeywordTrend
) {
    public record StatisticCard<T>(
            T value
    ) {
    }
}
