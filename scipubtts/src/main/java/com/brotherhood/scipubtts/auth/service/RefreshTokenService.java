package com.brotherhood.scipubtts.auth.service;

import com.brotherhood.scipubtts.auth.dto.response.RefreshTokenResult;
import com.brotherhood.scipubtts.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public interface RefreshTokenService {

    RefreshTokenResult issue(User user, boolean rememberMe, HttpServletRequest request);

    RefreshTokenResult rotate(String rawRefreshToken, HttpServletRequest request);

    void revokeAllByUserId(UUID userId);

    void revokeByRawToken(String rawRefreshToken);
}
