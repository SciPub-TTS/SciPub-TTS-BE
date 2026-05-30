package com.brotherhood.scipubtts.user.service;

import com.brotherhood.scipubtts.user.dto.request.ChangePasswordRequest;

import java.util.UUID;

public interface AccountService {
    void changePassword(UUID userId, ChangePasswordRequest request);
}
