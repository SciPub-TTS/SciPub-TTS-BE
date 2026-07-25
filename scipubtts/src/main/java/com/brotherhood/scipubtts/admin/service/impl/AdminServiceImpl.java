package com.brotherhood.scipubtts.admin.service.impl;

import com.brotherhood.scipubtts.admin.dto.AdminApiCallLogPageResponse;
import com.brotherhood.scipubtts.admin.dto.AdminApiCallConsumerResponse;
import com.brotherhood.scipubtts.admin.dto.AdminApiUsageDailyResponse;
import com.brotherhood.scipubtts.admin.dto.AdminDashboardStatisticsResponse;
import com.brotherhood.scipubtts.admin.dto.AdminOpenAlexFieldSummaryResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserActivityResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserDetailResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserDetailUserResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserBanSummaryResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserPageResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserProfileResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserSearchHistoryItemResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserSearchHistoryPageResponse;
import com.brotherhood.scipubtts.admin.repository.AdminDashboardRepository;
import com.brotherhood.scipubtts.admin.repository.OpenAlexSubfieldRepository;
import com.brotherhood.scipubtts.admin.repository.OpenAlexTopicRepository;
import com.brotherhood.scipubtts.admin.service.AdminService;
import com.brotherhood.scipubtts.admin.service.OpenAlexFieldTaxonomySyncService;
import com.brotherhood.scipubtts.auth.service.RefreshTokenService;
import com.brotherhood.scipubtts.bookmark.repository.UserBookmarkRepository;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.email.event.UserBannedEvent;
import com.brotherhood.scipubtts.email.event.UserUnbannedEvent;
import com.brotherhood.scipubtts.follow.entity.FollowTargetType;
import com.brotherhood.scipubtts.follow.repository.UserFollowRepository;
import com.brotherhood.scipubtts.dashboard.repository.KeywordTrendReadRepository;
import com.brotherhood.scipubtts.search.dto.response.RecentSearchResponse;
import com.brotherhood.scipubtts.search.repository.SearchHistoryRepository;
import com.brotherhood.scipubtts.dashboard.repository.TopicTrendReadRepository;
import com.brotherhood.scipubtts.user.entity.Role;
import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.entity.UserProfile;
import com.brotherhood.scipubtts.user.repository.UserProfileRepository;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final AdminDashboardRepository adminDashboardRepository;
    private final UserFollowRepository userFollowRepository;
    private final UserBookmarkRepository userBookmarkRepository;
    private final UserProfileRepository userProfileRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final KeywordTrendReadRepository keywordTrendReadRepository;
    private final TopicTrendReadRepository topicTrendReadRepository;
    private final OpenAlexSubfieldRepository openAlexSubfieldRepository;
    private final OpenAlexTopicRepository openAlexTopicRepository;
    private final OpenAlexFieldTaxonomySyncService openAlexFieldTaxonomySyncService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.dashboard.total-api-credit:100000}")
    private long totalApiCredit;

    @Override
    @Transactional(readOnly = true)
    public AdminUserPageResponse getAllUsers(int page, int size, String sort) {
        int safePage = Math.max(page, 0);
        int safeSize = normalizeSize(size);
        PageRequest pageRequest = PageRequest.of(safePage, safeSize, buildUserSort(sort));

        Page<User> userPage = userRepository.findAll(pageRequest);
        Map<UUID, FollowCounts> followCountsByUserId = getFollowCountsByUserId(userPage.getContent());
        List<AdminUserResponse> items = userPage.getContent()
                .stream()
                .map(user -> toResponse(user, followCountsByUserId.getOrDefault(user.getId(), FollowCounts.ZERO)))
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
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUserDetail(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UserProfile profile = userProfileRepository.findById(userId).orElse(null);

        return new AdminUserDetailResponse(
                toDetailUserResponse(user),
                toProfileResponse(profile),
                new AdminUserActivityResponse(
                        userFollowRepository.countByUserIdAndTargetType(userId, FollowTargetType.TOPIC),
                        userFollowRepository.countByUserIdAndTargetType(userId, FollowTargetType.AUTHOR),
                        userBookmarkRepository.countByUserId(userId),
                        searchHistoryRepository.countByUserId(userId)
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserSearchHistoryPageResponse getUserSearchHistory(UUID userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        int safePage = Math.max(page, 0);
        int safeSize = normalizeSize(size);
        Page<RecentSearchResponse> searchPage =
                searchHistoryRepository.findRecentDistinctSearchesByUserId(
                        userId,
                        PageRequest.of(safePage, safeSize)
                );
        List<AdminUserSearchHistoryItemResponse> items = searchPage.getContent()
                .stream()
                .map(search -> new AdminUserSearchHistoryItemResponse(
                        search.content(),
                        search.latestCreatedAt()
                ))
                .toList();

        return new AdminUserSearchHistoryPageResponse(
                items,
                searchPage.getNumber(),
                searchPage.getSize(),
                searchPage.getTotalElements(),
                searchPage.getTotalPages(),
                searchPage.hasNext()
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

        eventPublisher.publishEvent(new UserBannedEvent(user.getEmail()));

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

        eventPublisher.publishEvent(new UserUnbannedEvent(user.getEmail()));

        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardStatisticsResponse getDashboardStatistics() {
        OffsetDateTime now = OffsetDateTime.now();

        long totalUsers = adminDashboardRepository.countUsers();
        long bannedUsers = adminDashboardRepository.countBannedUsers();

        long totalSubfields = openAlexSubfieldRepository.count();
        long totalTopics = openAlexTopicRepository.count();
        long trendKeywordCount = countTrendKeywordsForLatestWeek();
        long trendTopicCount = countTrendTopicsForLatestWeek();

        return new AdminDashboardStatisticsResponse(
                card(totalUsers),
                card(bannedUsers),
                card(totalSubfields),
                card(totalTopics),
                card(trendTopicCount),
                card(trendKeywordCount)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOpenAlexFieldSummaryResponse getOpenAlexFieldSummary() {
        return new AdminOpenAlexFieldSummaryResponse(
                card(openAlexSubfieldRepository.count()),
                card(openAlexTopicRepository.count())
        );
    }

    @Override
    public AdminOpenAlexFieldSummaryResponse syncOpenAlexFieldSummary() {
        openAlexFieldTaxonomySyncService.syncFieldTaxonomy();
        return getOpenAlexFieldSummary();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserBanSummaryResponse getUserBanSummary() {
        long active = adminDashboardRepository.countActiveUsers();
        long banned = adminDashboardRepository.countBannedUsers();
        long total = active + banned;

        return new AdminUserBanSummaryResponse(
                card(active),
                card(banned),
                card(total)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminApiCallConsumerResponse> getTopApiConsumersThisMonth() {
        return adminDashboardRepository.findTopApiConsumersFromApiCallLog(
                startOfCurrentMonth(OffsetDateTime.now()),
                5
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminApiUsageDailyResponse> getApiUsageLast7Days() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate startDate = today.minusDays(6);

        return adminDashboardRepository.findApiUsageDailyFromApiCallLog(
                startDate,
                today
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminApiCallLogPageResponse getApiCallLogs(
            int page,
            int size,
            OffsetDateTime from,
            OffsetDateTime to,
            String callerType,
            UUID userId,
            String jobType,
            Integer status,
            String endpoint
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = normalizeSize(size);
        String normalizedCallerType = normalizeCallerType(callerType);

        if (from != null && to != null && !from.isBefore(to)) {
            throw new BusinessException(
                    ErrorCode.INVALID_SEARCH_FILTER_COMBINATION,
                    "from must be earlier than to"
            );
        }

        if (status != null && (status < 100 || status > 599)) {
            throw new BusinessException(
                    ErrorCode.INVALID_SEARCH_FILTER_COMBINATION,
                    "status must be a valid HTTP status code"
            );
        }

        return adminDashboardRepository.findApiCallLogs(
                safePage,
                safeSize,
                from,
                to,
                normalizedCallerType,
                userId,
                blankToNull(jobType),
                status,
                blankToNull(endpoint)
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
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private String normalizeCallerType(String callerType) {
        if (callerType == null || callerType.isBlank()) {
            return null;
        }

        String normalized = callerType.trim().toUpperCase();
        if (!normalized.equals("USER") && !normalized.equals("SYSTEM")) {
            throw new BusinessException(
                    ErrorCode.INVALID_SEARCH_FILTER_COMBINATION,
                    "callerType must be USER or SYSTEM"
            );
        }

        return normalized;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private AdminUserResponse toResponse(User user) {
        return toResponse(user, FollowCounts.ZERO);
    }

    private AdminUserResponse toResponse(User user, FollowCounts followCounts) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.isEmailVerified(),
                user.isGoogleLinked(),
                user.isBanned(),
                user.getCreatedAt(),
                followCounts.topicCount(),
                followCounts.authorCount()
        );
    }

    private AdminUserDetailUserResponse toDetailUserResponse(User user) {
        return new AdminUserDetailUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getAvatarUrl(),
                user.getRole().name(),
                user.isEmailVerified(),
                user.isGoogleLinked(),
                user.isBanned(),
                user.getCreatedAt()
        );
    }

    private AdminUserProfileResponse toProfileResponse(UserProfile profile) {
        if (profile == null) {
            return new AdminUserProfileResponse(null, null, null, null, null);
        }

        return new AdminUserProfileResponse(
                profile.getInstitution(),
                profile.getDepartment(),
                profile.getCountry(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    private Map<UUID, FollowCounts> getFollowCountsByUserId(List<User> users) {
        if (users.isEmpty()) {
            return Map.of();
        }

        List<UUID> userIds = users.stream()
                .map(User::getId)
                .toList();
        List<FollowTargetType> targetTypes = List.of(FollowTargetType.TOPIC, FollowTargetType.AUTHOR);
        Map<UUID, FollowCounts> countsByUserId = new HashMap<>();

        userFollowRepository.countByUserIdsAndTargetTypes(userIds, targetTypes)
                .forEach(count -> {
                    FollowCounts current = countsByUserId.getOrDefault(count.getUserId(), FollowCounts.ZERO);
                    FollowCounts updated = switch (count.getTargetType()) {
                        case TOPIC -> new FollowCounts(count.getCount(), current.authorCount());
                        case AUTHOR -> new FollowCounts(current.topicCount(), count.getCount());
                        default -> current;
                    };
                    countsByUserId.put(count.getUserId(), updated);
                });

        return countsByUserId;
    }

    private long countTrendKeywordsForLatestWeek() {
        LocalDate latestSnapshotDate = keywordTrendReadRepository.findLatestSnapshotDate();
        if (latestSnapshotDate == null) {
            return 0L;
        }

        return keywordTrendReadRepository.countTrendingKeywords(latestSnapshotDate);
    }

    private long countTrendTopicsForLatestWeek() {
        LocalDate latestSnapshotDate = topicTrendReadRepository.findLatestSnapshotDate();
        if (latestSnapshotDate == null) {
            return 0L;
        }

        return topicTrendReadRepository.countTrendingTopics(latestSnapshotDate);
    }

    private <T> AdminDashboardStatisticsResponse.StatisticCard<T> card(T value) {
        return new AdminDashboardStatisticsResponse.StatisticCard<>(value);
    }

    private OffsetDateTime startOfCurrentMonth(OffsetDateTime now) {
        LocalDate date = now.toLocalDate().withDayOfMonth(1);
        return date.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
    }

    private OffsetDateTime startOfToday(OffsetDateTime now) {
        return now.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private record FollowCounts(long topicCount, long authorCount) {
        private static final FollowCounts ZERO = new FollowCounts(0, 0);
    }
}
