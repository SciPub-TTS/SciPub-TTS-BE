package com.brotherhood.scipubtts.admin.service.impl;

import com.brotherhood.scipubtts.admin.dto.AdminApiCallConsumerResponse;
import com.brotherhood.scipubtts.admin.dto.AdminApiUsageDailyResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserPageResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserResponse;
import com.brotherhood.scipubtts.admin.repository.AdminDashboardRepository;
import com.brotherhood.scipubtts.auth.service.RefreshTokenService;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.user.entity.Role;
import com.brotherhood.scipubtts.user.entity.User;
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

    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminServiceImpl(userRepository, refreshTokenService, adminDashboardRepository);
    }

    @Test
    void getAllUsersReturnsPagedUsers() {
        User first = researcher(false);
        User second = researcher(true);
        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));

        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(first, second), pageRequest, 3));

        AdminUserPageResponse response = adminService.getAllUsers(0, 2, "RECENT");

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().getFirst().id()).isEqualTo(first.getId());
        assertThat(response.items().getFirst().email()).isEqualTo(first.getEmail());
        assertThat(response.items().get(1).banned()).isTrue();
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
}
