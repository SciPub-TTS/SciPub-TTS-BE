package com.brotherhood.scipubtts.admin.dto;

public record AdminUserBanSummaryResponse(
        AdminDashboardStatisticsResponse.StatisticCard<Long> active,
        AdminDashboardStatisticsResponse.StatisticCard<Long> banned,
        AdminDashboardStatisticsResponse.StatisticCard<Long> total
) {
}
