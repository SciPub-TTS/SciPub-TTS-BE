package com.brotherhood.scipubtts.admin.service.impl;

import com.brotherhood.scipubtts.admin.dto.AdminApiCallConsumerResponse;
import com.brotherhood.scipubtts.admin.dto.AdminApiUsageDailyResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserDetailResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserPageResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserResponse;
import com.brotherhood.scipubtts.admin.repository.AdminDashboardRepository;
import com.brotherhood.scipubtts.auth.service.RefreshTokenService;
import com.brotherhood.scipubtts.bookmark.repository.UserBookmarkRepository;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.follow.entity.FollowTargetType;
import com.brotherhood.scipubtts.follow.repository.UserFollowRepository;
import com.brotherhood.scipubtts.search.repository.SearchHistoryRepository;
import com.brotherhood.scipubtts.user.entity.Role;
import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.entity.UserProfile;
import com.brotherhood.scipubtts.user.repository.UserProfileRepository;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AdminDashboardRepository adminDashboardRepository;

    @Mock
    private UserFollowRepository userFollowRepository;

    @Mock
    private UserBookmarkRepository userBookmarkRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private SearchHistoryRepository searchHistoryRepository;

    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminServiceImpl(
                userRepository,
                refreshTokenService,
                adminDashboardRepository,
                userFollowRepository,
                userBookmarkRepository,
                userProfileRepository,
                searchHistoryRepository
        );
    }

    @Test
    void getAllUsersReturnsPagedUsers() {
        User first = researcher(false);
        User second = researcher(true);
        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));

        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(first, second), pageRequest, 3));
        when(userFollowRepository.countByUserIdsAndTargetTypes(anyList(), anyList()))
                .thenReturn(List.of(
                        followCount(first.getId(), FollowTargetType.TOPIC, 3),
                        followCount(first.getId(), FollowTargetType.AUTHOR, 5),
                        followCount(second.getId(), FollowTargetType.AUTHOR, 2)
                ));

        AdminUserPageResponse response = adminService.getAllUsers(0, 2, "RECENT");

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().getFirst().id()).isEqualTo(first.getId());
        assertThat(response.items().getFirst().email()).isEqualTo(first.getEmail());
        assertThat(response.items().getFirst().topicCount()).isEqualTo(3);
        assertThat(response.items().getFirst().authorCount()).isEqualTo(5);
        assertThat(response.items().get(1).banned()).isTrue();
        assertThat(response.items().get(1).topicCount()).isZero();
        assertThat(response.items().get(1).authorCount()).isEqualTo(2);
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(3);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    void getAllUsersFallsBackToRecentSort() {
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        adminService.getAllUsers(0, 10, "UNKNOWN");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(pageableCaptor.capture());

        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getAllUsersReturnsZeroFollowCountsWhenUserHasNoFollows() {
        User user = researcher(false);
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"));

        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user), pageRequest, 1));
        when(userFollowRepository.countByUserIdsAndTargetTypes(anyList(), anyList()))
                .thenReturn(List.of());

        AdminUserPageResponse response = adminService.getAllUsers(0, 1, "RECENT");

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().topicCount()).isZero();
        assertThat(response.items().getFirst().authorCount()).isZero();
    }

    @Test
    void getAllUsersQueriesFollowCountsForUsersInCurrentPageOnly() {
        User first = researcher(false);
        User second = researcher(false);
        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));

        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(first, second), pageRequest, 4));
        when(userFollowRepository.countByUserIdsAndTargetTypes(anyList(), anyList()))
                .thenReturn(List.of());

        adminService.getAllUsers(0, 2, "RECENT");

        ArgumentCaptor<List<UUID>> userIdsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<FollowTargetType>> targetTypesCaptor = ArgumentCaptor.forClass(List.class);
        verify(userFollowRepository).countByUserIdsAndTargetTypes(
                userIdsCaptor.capture(),
                targetTypesCaptor.capture()
        );

        assertThat(userIdsCaptor.getValue()).containsExactly(first.getId(), second.getId());
        assertThat(targetTypesCaptor.getValue()).containsExactly(FollowTargetType.TOPIC, FollowTargetType.AUTHOR);
    }

    @Test
    void getAllUsersClampsInvalidPageAndSize() {
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));

        adminService.getAllUsers(-1, 200, null);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getUserDetailReturnsProfileAndActivityCounts() {
        User user = researcher(false);
        user.setUsername("researcher-one");
        user.setAvatarUrl("https://example.com/avatar.png");
        UserProfile profile = profile(user, "FPT University", "AI Lab", "Vietnam");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userProfileRepository.findById(user.getId())).thenReturn(Optional.of(profile));
        when(userFollowRepository.countByUserIdAndTargetType(user.getId(), FollowTargetType.TOPIC)).thenReturn(3L);
        when(userFollowRepository.countByUserIdAndTargetType(user.getId(), FollowTargetType.AUTHOR)).thenReturn(5L);
        when(userBookmarkRepository.countByUserId(user.getId())).thenReturn(7L);
        when(searchHistoryRepository.countByUserId(user.getId())).thenReturn(9L);

        AdminUserDetailResponse response = adminService.getUserDetail(user.getId());

        assertThat(response.user().id()).isEqualTo(user.getId());
        assertThat(response.user().username()).isEqualTo("researcher-one");
        assertThat(response.user().avatarUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(response.profile().institution()).isEqualTo("FPT University");
        assertThat(response.profile().department()).isEqualTo("AI Lab");
        assertThat(response.profile().country()).isEqualTo("Vietnam");
        assertThat(response.activity().topicCount()).isEqualTo(3);
        assertThat(response.activity().authorCount()).isEqualTo(5);
        assertThat(response.activity().bookmarkCount()).isEqualTo(7);
        assertThat(response.activity().searchCount()).isEqualTo(9);
    }

    @Test
    void getUserDetailReturnsNullableProfileAndZeroCountsWhenProfileDoesNotExist() {
        User user = researcher(false);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userProfileRepository.findById(user.getId())).thenReturn(Optional.empty());

        AdminUserDetailResponse response = adminService.getUserDetail(user.getId());

        assertThat(response.profile().institution()).isNull();
        assertThat(response.profile().department()).isNull();
        assertThat(response.profile().country()).isNull();
        assertThat(response.profile().createdAt()).isNull();
        assertThat(response.profile().updatedAt()).isNull();
        assertThat(response.activity().topicCount()).isZero();
        assertThat(response.activity().authorCount()).isZero();
        assertThat(response.activity().bookmarkCount()).isZero();
        assertThat(response.activity().searchCount()).isZero();
    }

    @Test
    void getUserDetailThrowsWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.getUserDetail(userId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void getUserSearchHistoryReturnsPagedRecentDistinctSearches() {
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        PageRequest pageRequest = PageRequest.of(0, 2);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(searchHistoryRepository.findRecentDistinctSearchesByUserId(userId, pageRequest))
                .thenReturn(new PageImpl<>(
                        List.of(
                                recentSearch("machine learning", now),
                                recentSearch("science education", now.minusDays(1))
                        ),
                        pageRequest,
                        3
                ));

        var response = adminService.getUserSearchHistory(userId, 0, 2);

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().getFirst().keyword()).isEqualTo("machine learning");
        assertThat(response.items().getFirst().searchedAt()).isEqualTo(now);
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(3);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    void getUserSearchHistoryClampsInvalidPageAndSize() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(searchHistoryRepository.findRecentDistinctSearchesByUserId(any(UUID.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));

        adminService.getUserSearchHistory(userId, -1, 200);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(searchHistoryRepository).findRecentDistinctSearchesByUserId(
                any(UUID.class),
                pageableCaptor.capture()
        );

        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    void getTopApiConsumersThisMonthReturnsRepositoryResults() {
        List<AdminApiCallConsumerResponse> consumers = List.of(
                new AdminApiCallConsumerResponse("researcher01@email.com", 12),
                new AdminApiCallConsumerResponse("student02@email.com", 8)
        );

        when(adminDashboardRepository.findTopApiConsumersFromSearchHistory(
                any(OffsetDateTime.class),
                any(Integer.class)
        )).thenReturn(consumers);

        List<AdminApiCallConsumerResponse> response = adminService.getTopApiConsumersThisMonth();

        assertThat(response).isEqualTo(consumers);
    }

    @Test
    void getTopApiConsumersThisMonthUsesStartOfCurrentMonthAndTopFiveLimit() {
        when(adminDashboardRepository.findTopApiConsumersFromSearchHistory(
                any(OffsetDateTime.class),
                any(Integer.class)
        )).thenReturn(List.of());

        adminService.getTopApiConsumersThisMonth();

        ArgumentCaptor<OffsetDateTime> fromCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<Integer> limitCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(adminDashboardRepository).findTopApiConsumersFromSearchHistory(
                fromCaptor.capture(),
                limitCaptor.capture()
        );

        OffsetDateTime from = fromCaptor.getValue();
        assertThat(from.getDayOfMonth()).isEqualTo(1);
        assertThat(from.getHour()).isZero();
        assertThat(from.getMinute()).isZero();
        assertThat(from.getSecond()).isZero();
        assertThat(limitCaptor.getValue()).isEqualTo(5);
    }

    @Test
    void getApiUsageLast7DaysReturnsRepositoryResults() {
        List<AdminApiUsageDailyResponse> usage = List.of(
                new AdminApiUsageDailyResponse(LocalDate.now().minusDays(1), 4),
                new AdminApiUsageDailyResponse(LocalDate.now(), 9)
        );

        when(adminDashboardRepository.findApiUsageDailyFromSearchHistory(
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(usage);

        List<AdminApiUsageDailyResponse> response = adminService.getApiUsageLast7Days();

        assertThat(response).isEqualTo(usage);
    }

    @Test
    void getApiUsageLast7DaysUsesSevenDayWindowIncludingToday() {
        when(adminDashboardRepository.findApiUsageDailyFromSearchHistory(
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(List.of());

        adminService.getApiUsageLast7Days();

        ArgumentCaptor<LocalDate> startDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(adminDashboardRepository).findApiUsageDailyFromSearchHistory(
                startDateCaptor.capture(),
                endDateCaptor.capture()
        );

        assertThat(startDateCaptor.getValue()).isEqualTo(endDateCaptor.getValue().minusDays(6));
        assertThat(endDateCaptor.getValue()).isEqualTo(LocalDate.now());
    }

    @Test
    void banUserSetsBannedAndRevokesRefreshTokens() {
        UUID adminId = UUID.randomUUID();
        User target = researcher(false);

        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));

        AdminUserResponse response = adminService.banUser(adminId, target.getId());

        assertThat(target.isBanned()).isTrue();
        assertThat(response.banned()).isTrue();
        verify(userRepository).save(target);
        verify(refreshTokenService).revokeAllByUserId(target.getId());
    }

    @Test
    void banUserThrowsWhenAccountAlreadyBanned() {
        UUID adminId = UUID.randomUUID();
        User target = researcher(true);

        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> adminService.banUser(adminId, target.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ADMIN_ACCOUNT_ALREADY_BANNED);

        verify(userRepository, never()).save(target);
        verify(refreshTokenService, never()).revokeAllByUserId(target.getId());
    }

    @Test
    void unbanUserSetsBannedFalse() {
        UUID adminId = UUID.randomUUID();
        User target = researcher(true);

        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));

        AdminUserResponse response = adminService.unbanUser(adminId, target.getId());

        assertThat(target.isBanned()).isFalse();
        assertThat(response.banned()).isFalse();
        verify(userRepository).save(target);
        verify(refreshTokenService, never()).revokeAllByUserId(target.getId());
    }

    @Test
    void unbanUserThrowsWhenAccountIsNotBanned() {
        UUID adminId = UUID.randomUUID();
        User target = researcher(false);

        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> adminService.unbanUser(adminId, target.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ADMIN_ACCOUNT_NOT_BANNED);

        verify(userRepository, never()).save(target);
        verify(refreshTokenService, never()).revokeAllByUserId(target.getId());
    }

    @Test
    void cannotBanSelf() {
        UUID adminId = UUID.randomUUID();

        assertThatThrownBy(() -> adminService.banUser(adminId, adminId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ADMIN_SELF_ACTION_NOT_ALLOWED);
    }

    @Test
    void cannotBanAdminTarget() {
        UUID adminId = UUID.randomUUID();
        User target = admin();

        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> adminService.banUser(adminId, target.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ADMIN_TARGET_NOT_ALLOWED);
    }

    @Test
    void missingUserThrowsUserNotFound() {
        UUID adminId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        when(userRepository.findById(targetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.banUser(adminId, targetId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    private User researcher(boolean banned) {
        return user(Role.RESEARCHER, banned);
    }

    private User admin() {
        return user(Role.ADMIN, false);
    }

    private User user(Role role, boolean banned) {
        return User.builder()
                .id(UUID.randomUUID())
                .email(UUID.randomUUID() + "@example.com")
                .firstName("First")
                .lastName("Last")
                .role(role)
                .emailVerified(true)
                .googleLinked(false)
                .banned(banned)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    private UserProfile profile(User user, String institution, String department, String country) {
        OffsetDateTime now = OffsetDateTime.now();
        return UserProfile.builder()
                .userId(user.getId())
                .user(user)
                .institution(institution)
                .department(department)
                .country(country)
                .createdAt(now.minusDays(1))
                .updatedAt(now)
                .build();
    }

    private UserFollowRepository.UserFollowCountView followCount(
            UUID userId,
            FollowTargetType targetType,
            long count
    ) {
        return new UserFollowRepository.UserFollowCountView() {
            @Override
            public UUID getUserId() {
                return userId;
            }

            @Override
            public FollowTargetType getTargetType() {
                return targetType;
            }

            @Override
            public long getCount() {
                return count;
            }
        };
    }

    private SearchHistoryRepository.RecentSearchProjection recentSearch(
            String content,
            OffsetDateTime latestCreatedAt
    ) {
        return new SearchHistoryRepository.RecentSearchProjection() {
            @Override
            public String getContent() {
                return content;
            }

            @Override
            public OffsetDateTime getLatestCreatedAt() {
                return latestCreatedAt;
            }
        };
    }
}
