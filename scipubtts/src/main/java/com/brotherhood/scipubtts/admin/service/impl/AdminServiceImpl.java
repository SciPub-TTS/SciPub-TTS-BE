package com.brotherhood.scipubtts.admin.service.impl;

import com.brotherhood.scipubtts.admin.dto.AdminApiCallConsumerResponse;
import com.brotherhood.scipubtts.admin.dto.AdminApiUsageDailyResponse;
import com.brotherhood.scipubtts.admin.dto.AdminDashboardStatisticsResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserBanSummaryResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserPageResponse;
import com.brotherhood.scipubtts.admin.repository.AdminDashboardRepository;
import com.brotherhood.scipubtts.admin.service.AdminService;
import com.brotherhood.scipubtts.auth.service.RefreshTokenService;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.user.entity.Role;
import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final AdminDashboardRepository adminDashboardRepository;

    @Value("${app.dashboard.total-api-credit:100000}")
    private long totalApiCredit;

    @Override
    @Transactional(readOnly = true)
    public AdminUserPageResponse getAllUsers(int page, int size, String sort) {
        int safePage = Math.max(page, 0);
        int safeSize = normalizeSize(size);
        PageRequest pageRequest = PageRequest.of(safePage, safeSize, buildUserSort(sort));

        Page<User> userPage = userRepository.findAll(pageRequest);
        List<AdminUserResponse> items = userPage.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return new AdminUserPageResponse(
                items,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.hasNext()
        );
    }

    @Override
    @Transactional
    public AdminUserResponse banUser(UUID adminId, UUID userId) {
        User user = getValidTargetUser(adminId, userId);

        if (user.isBanned()) {
            throw new BusinessException(ErrorCode.ADMIN_ACCOUNT_ALREADY_BANNED);
        }

        user.setBanned(true);
        userRepository.save(user);
        refreshTokenService.revokeAllByUserId(user.getId());

        return toResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse unbanUser(UUID adminId, UUID userId) {
        User user = getValidTargetUser(adminId, userId);

        if (!user.isBanned()) {
            throw new BusinessException(ErrorCode.ADMIN_ACCOUNT_NOT_BANNED);
        }

        user.setBanned(false);
        userRepository.save(user);

        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardStatisticsResponse getDashboardStatistics() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime startOfWeek = startOfCurrentWeek(now);
        OffsetDateTime startOfMonth = startOfCurrentMonth(now);
        OffsetDateTime startOfPreviousMonth = startOfMonth.minusMonths(1);
        OffsetDateTime startOfToday = startOfToday(now);

        long totalUsers = adminDashboardRepository.countUsers();
        long usersThisWeek = adminDashboardRepository.countUsersCreatedFrom(startOfWeek);

        long bannedUsers = adminDashboardRepository.countBannedUsers();
        long bannedUsersThisMonth = adminDashboardRepository.countBannedUsersCreatedFrom(startOfMonth);

        long apiCallsThisMonth = adminDashboardRepository.countApiCallsFrom(startOfMonth);
        long apiCallsPreviousMonth = adminDashboardRepository.countApiCallsBetween(startOfPreviousMonth, startOfMonth);
        long apiCallsToday = adminDashboardRepository.countApiCallsFrom(startOfToday);

        long totalSubfields = adminDashboardRepository.countSubfields();
        long totalFields = adminDashboardRepository.countFields();
        long totalTopics = adminDashboardRepository.countTopics();
        long topicsLatestPeriod = adminDashboardRepository.countTopicsForLatestPeriod();
        long topicsPreviousPeriod = adminDashboardRepository.countTopicsForPreviousPeriod();
        long activeTrends = adminDashboardRepository.countActiveTrendsForLatestPeriod();
        long previousActiveTrends = adminDashboardRepository.countActiveTrendsForPreviousPeriod();

        return new AdminDashboardStatisticsResponse(
                card(totalUsers, "Registered accounts", signedCount(usersThisWeek, "this week")),
                card(activeTrends, "Detected trend signals", signedCount(activeTrends - previousActiveTrends, "this week")),
                card(bannedUsers, "Restricted accounts", signedCount(bannedUsersThisMonth, "this month")),
                card(apiCallsThisMonth, "This month", percentageDelta(apiCallsThisMonth, apiCallsPreviousMonth)),
                card(apiCallsToday, "Today", "Within daily quota"),
                card(totalApiCredit, "Daily usage", "All"),
                card(totalSubfields, "Research subfields", null),
                card(totalTopics, "Generated from " + totalFields + " fields", signedCount(topicsLatestPeriod - topicsPreviousPeriod, "after last sync")),
                card(adminDashboardRepository.findLatestSynchronization().orElse(null), "Latest data update", null)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserBanSummaryResponse getUserBanSummary() {
        long active = adminDashboardRepository.countActiveUsers();
        long banned = adminDashboardRepository.countBannedUsers();
        long total = active + banned;

        int activePercentage = 0;
        int bannedPercentage = 0;

        if (total > 0) {
            activePercentage = (int) Math.round(active * 100.0 / total);
            bannedPercentage = 100 - activePercentage;
        }

        return new AdminUserBanSummaryResponse(
                active,
                banned,
                total,
                activePercentage,
                bannedPercentage
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminApiCallConsumerResponse> getTopApiConsumersThisMonth() {
        return adminDashboardRepository.findTopApiConsumersFromSearchHistory(
                startOfCurrentMonth(OffsetDateTime.now()),
                5
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminApiUsageDailyResponse> getApiUsageLast7Days() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate startDate = today.minusDays(6);

        return adminDashboardRepository.findApiUsageDailyFromSearchHistory(
                startDate,
                today
        );
    }

    private User getValidTargetUser(UUID adminId, UUID userId) {
        if (Objects.equals(adminId, userId)) {
            throw new BusinessException(ErrorCode.ADMIN_SELF_ACTION_NOT_ALLOWED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (Role.ADMIN.equals(user.getRole())) {
            throw new BusinessException(ErrorCode.ADMIN_TARGET_NOT_ALLOWED);
        }

        return user;
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 10;
        }

        return Math.min(size, 100);
    }

    private Sort buildUserSort(String sort) {
        if (sort == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        return switch (sort.trim().toUpperCase()) {
            case "OLDEST" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "EMAIL_ASC" -> Sort.by(Sort.Direction.ASC, "email");
            case "EMAIL_DESC" -> Sort.by(Sort.Direction.DESC, "email");
            case "RECENT" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.isEmailVerified(),
                user.isGoogleLinked(),
                user.isBanned(),
                user.getCreatedAt()
        );
    }

    private <T> AdminDashboardStatisticsResponse.StatisticCard<T> card(
            T value,
            String description,
            String delta
    ) {
        return new AdminDashboardStatisticsResponse.StatisticCard<>(value, description, delta);
    }

    private OffsetDateTime startOfCurrentWeek(OffsetDateTime now) {
        LocalDate date = now.toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return date.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private OffsetDateTime startOfCurrentMonth(OffsetDateTime now) {
        LocalDate date = now.toLocalDate().withDayOfMonth(1);
        return date.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private OffsetDateTime startOfToday(OffsetDateTime now) {
        return now.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private String signedCount(long value, String suffix) {
        String sign = value >= 0 ? "+" : "";
        return sign + value + " " + suffix;
    }

    private String percentageDelta(long current, long previous) {
        if (previous == 0) {
            return current == 0 ? "0% vs last month" : "+100% vs last month";
        }

        long percent = Math.round(((double) (current - previous) / previous) * 100);
        String sign = percent >= 0 ? "+" : "";
        return sign + percent + "% vs last month";
    }
}
