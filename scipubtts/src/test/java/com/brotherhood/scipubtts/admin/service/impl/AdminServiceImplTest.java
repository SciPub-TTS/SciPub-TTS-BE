package com.brotherhood.scipubtts.admin.service.impl;

import com.brotherhood.scipubtts.admin.dto.AdminUserResponse;
import com.brotherhood.scipubtts.auth.service.RefreshTokenService;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.user.entity.Role;
import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminServiceImpl(userRepository, refreshTokenService);
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
        User target = admin(false);

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

    private User admin(boolean banned) {
        return user(Role.ADMIN, banned);
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
