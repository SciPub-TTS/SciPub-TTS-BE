package com.brotherhood.scipubtts.user.service;

import com.brotherhood.scipubtts.auth.dto.response.CurrentUserResponse;
import com.brotherhood.scipubtts.user.dto.request.ChangePasswordRequest;
import com.brotherhood.scipubtts.user.dto.request.UpdateUserProfileRequest;

import java.util.UUID;

public interface AccountService {
    void changePassword(UUID userId, ChangePasswordRequest request);

    CurrentUserResponse getCurrentUser(UUID userId);

    CurrentUserResponse updateProfile(UUID userId, UpdateUserProfileRequest request);
}
