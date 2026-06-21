package com.brotherhood.scipubtts.user.service.impl;

import com.brotherhood.scipubtts.auth.dto.response.CurrentUserResponse;
import com.brotherhood.scipubtts.auth.service.RefreshTokenService;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.user.dto.request.ChangePasswordRequest;
import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import com.brotherhood.scipubtts.user.service.AccountService;
import com.brotherhood.scipubtts.user.service.AvatarService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AvatarService avatarService;

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {

        if (!Objects.equals(request.newPassword(), request.confirmNewPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_NOT_MATCH);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Temporarily disable the "set password for Google-only account" flow
        // while login/register is being tested in isolation.
        //
        // boolean hasLocalPassword = StringUtils.hasText(user.getPasswordHash());
        //
        // if (hasLocalPassword) {
        //     if (!StringUtils.hasText(request.currentPassword())
        //             || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
        //         throw new BusinessException(ErrorCode.CURRENT_PASSWORD_INVALID);
        //     }
        //
        //     if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
        //         throw new BusinessException(ErrorCode.PASSWORD_REUSE_NOT_ALLOWED);
        //     }
        // }

        if (!StringUtils.hasText(user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOCAL_PASSWORD_NOT_AVAILABLE);
        }

        if (!StringUtils.hasText(request.currentPassword())
                || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.CURRENT_PASSWORD_INVALID);
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_REUSE_NOT_ALLOWED);
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        refreshTokenService.revokeAllByUserId(userId);
    }

    @Override
    public CurrentUserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!StringUtils.hasText(user.getAvatarUrl())) {
            user.setAvatarUrl(avatarService.buildDefaultAvatarUrl(user));
            userRepository.save(user);
        }

        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatarUrl(),
                user.getRole().name(),
                user.isGoogleLinked(),
                StringUtils.hasText(user.getPasswordHash())
        );
    }

}
