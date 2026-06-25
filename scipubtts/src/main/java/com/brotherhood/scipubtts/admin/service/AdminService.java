package com.brotherhood.scipubtts.admin.service;

import com.brotherhood.scipubtts.admin.dto.AdminApiCallConsumerResponse;
import com.brotherhood.scipubtts.admin.dto.AdminApiUsageDailyResponse;
import com.brotherhood.scipubtts.admin.dto.AdminDashboardStatisticsResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserDetailResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserBanSummaryResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserPageResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserSearchHistoryPageResponse;

import java.util.List;
import java.util.UUID;

public interface AdminService {

    AdminUserResponse banUser(UUID adminId, UUID userId);

    AdminUserResponse unbanUser(UUID adminId, UUID userId);

    AdminUserPageResponse getAllUsers(int page, int size, String sort);

    AdminUserDetailResponse getUserDetail(UUID userId);

    AdminUserSearchHistoryPageResponse getUserSearchHistory(UUID userId, int page, int size);

    AdminDashboardStatisticsResponse getDashboardStatistics();

    AdminUserBanSummaryResponse getUserBanSummary();

    List<AdminApiCallConsumerResponse> getTopApiConsumersThisMonth();

    List<AdminApiUsageDailyResponse> getApiUsageLast7Days();
}
