package com.brotherhood.scipubtts.admin.service.impl;

import com.brotherhood.scipubtts.admin.dto.AdminUserResponse;
import com.brotherhood.scipubtts.admin.service.AdminService;
import com.brotherhood.scipubtts.auth.service.RefreshTokenService;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.user.entity.Role;
import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

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
}
