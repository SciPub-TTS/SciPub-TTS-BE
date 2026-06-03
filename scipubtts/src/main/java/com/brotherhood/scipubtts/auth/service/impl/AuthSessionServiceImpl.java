package com.brotherhood.scipubtts.auth.service.impl;

import com.brotherhood.scipubtts.auth.dto.response.AuthResponse;
import com.brotherhood.scipubtts.auth.dto.response.RefreshTokenResult;
import com.brotherhood.scipubtts.auth.security.UserPrincipal;
import com.brotherhood.scipubtts.auth.security.jwt.JwtTokenService;
import com.brotherhood.scipubtts.auth.service.AuthSessionService;
import com.brotherhood.scipubtts.auth.service.RefreshCookieService;
import com.brotherhood.scipubtts.auth.service.RefreshTokenService;
import com.brotherhood.scipubtts.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;

@Service
public class AuthSessionServiceImpl implements AuthSessionService {


    private final RefreshTokenService refreshTokenService;
    private final RefreshCookieService refreshCookieService;
    private final JwtTokenService jwtTokenService;

    public AuthSessionServiceImpl(
            RefreshTokenService refreshTokenService,
            RefreshCookieService refreshCookieService,
            JwtTokenService jwtTokenService
    ) {
        this.refreshTokenService = refreshTokenService;
        this.refreshCookieService = refreshCookieService;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public AuthResponse issueSession(
            User user,
            boolean rememberMe,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        RefreshTokenResult refreshResult =
                refreshTokenService.issue(user, rememberMe, request);

        refreshCookieService.addRefreshCookie(
                response,
                refreshResult.rawToken(),
                refreshResult.rememberMe(),
                Duration.between(OffsetDateTime.now(), refreshResult.expiresAt())
        );

        String accessToken =
                jwtTokenService.generateAccessToken(UserPrincipal.create(user));

        return new AuthResponse(
                accessToken,
                "Bearer",
                jwtTokenService.getAccessTokenExpiresInSeconds()
        );
    }
}
