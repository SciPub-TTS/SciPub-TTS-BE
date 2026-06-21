package com.brotherhood.scipubtts.user.service.impl;

import com.brotherhood.scipubtts.auth.dto.response.CurrentUserResponse;
import com.brotherhood.scipubtts.auth.service.RefreshTokenService;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.user.dto.request.ChangePasswordRequest;
import com.brotherhood.scipubtts.user.dto.request.UpdateUserProfileRequest;
import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.entity.UserProfile;
import com.brotherhood.scipubtts.user.repository.UserProfileRepository;
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
    private final UserProfileRepository userProfileRepository;
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
    @Transactional
    public CurrentUserResponse updateProfile(UUID userId, UpdateUserProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (request.firstName() != null) {
            user.setFirstName(request.firstName().trim());
        }

        if (request.lastName() != null) {
            user.setLastName(request.lastName().trim());
        }

        user = userRepository.save(user);

        User finalUser = user;
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> UserProfile.builder()
                        .userId(userId)
                        .user(finalUser)
                        .build());

        if (request.institution() != null) {
            profile.setInstitution(request.institution().trim());
        }

        if (request.department() != null) {
            profile.setDepartment(request.department().trim());
        }

        if (request.country() != null) {
            profile.setCountry(request.country().trim());
        }

        profile = userProfileRepository.save(profile);

        return toCurrentUserResponse(user, profile);
    }

    @Override
    @Transactional
    public CurrentUserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!StringUtils.hasText(user.getAvatarUrl())) {
            user.setAvatarUrl(avatarService.buildDefaultAvatarUrl(user));
            userRepository.save(user);
        }

        UserProfile profile = userProfileRepository.findById(userId).orElse(null);

        return toCurrentUserResponse(user, profile);
    }

    private CurrentUserResponse toCurrentUserResponse(User user, UserProfile profile) {
        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatarUrl(),
                user.getRole().name(),
                user.isGoogleLinked(),
                profile != null ? profile.getInstitution() : null,
                profile != null ? profile.getDepartment() : null,
                profile != null ? profile.getCountry() : "Vietnam"
        );
    }
}
