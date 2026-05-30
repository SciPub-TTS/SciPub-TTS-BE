package com.brotherhood.scipubtts.auth.service;

import com.brotherhood.scipubtts.auth.dto.request.LoginRequest;
import com.brotherhood.scipubtts.auth.dto.request.RegisterLocalRequest;
import com.brotherhood.scipubtts.auth.dto.response.AuthResponse;
import com.brotherhood.scipubtts.auth.dto.response.RefreshTokenResult;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.email.service.EmailService;
import com.brotherhood.scipubtts.email.entity.EmailVerificationToken;
import com.brotherhood.scipubtts.email.repository.EmailVerificationTokenRepository;
import com.brotherhood.scipubtts.auth.security.UserPrincipal;
import com.brotherhood.scipubtts.auth.security.jwt.JwtTokenService;
import com.brotherhood.scipubtts.user.entity.Role;
import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final EmailService mailService;

    private final RefreshTokenService refreshTokenService;
    private final RefreshCookieService refreshCookieService;

    @Value("${app.backend-base-url:http://localhost:8080}")
    private String backendBaseUrl;

    @Override
    @Transactional
    public String registerLocal(RegisterLocalRequest request) {
        // giữ nguyên logic register hiện tại của bạn
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Email already exists");
        }

        User user = User.builder()
                .email(request.email())
                .username(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.RESEARCHER)
                .emailVerified(false)
                .googleLinked(false)
                .banned(false)
                .build();

        userRepository.save(user);

        String rawToken = UUID.randomUUID().toString();
        String redirectUrl = buildRedirectUrl(request.appBaseUrl());

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .token(rawToken)
                .expiresAt(OffsetDateTime.now().plusHours(24))
                .redirectUrl(redirectUrl)
                .build();

        tokenRepository.save(verificationToken);

        String verifyLink = backendBaseUrl + "/api/auth/verify-email?token=" + rawToken;
        mailService.sendVerificationEmail(user.getEmail(), verifyLink);

        return "Register success. Please check your email to verify your account.";
    }

    @Override
    @Transactional
    public String verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (verificationToken.isUsed()) {
            throw new RuntimeException("Verification token already used");
        }

        if (verificationToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new RuntimeException("Verification token expired");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        verificationToken.setUsedAt(OffsetDateTime.now());

        userRepository.save(user);
        tokenRepository.save(verificationToken);

        return verificationToken.getRedirectUrl();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request,
                              HttpServletRequest httpRequest,
                              HttpServletResponse httpResponse) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (user.isBanned()) {
            throw new BusinessException(ErrorCode.ACCOUNT_BANNED);
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new BusinessException(ErrorCode.LOCAL_PASSWORD_NOT_AVAILABLE);
        }

        if (!user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.email(),
                        request.password()
                )
        );

        RefreshTokenResult refreshResult =
                refreshTokenService.issue(user, request.rememberMe(), httpRequest);

        refreshCookieService.addRefreshCookie(
                httpResponse,
                refreshResult.rawToken(),
                refreshResult.rememberMe(),
                Duration.between(OffsetDateTime.now(), refreshResult.expiresAt())
        );

        String accessToken = jwtTokenService.generateAccessToken(UserPrincipal.create(user));

        return new AuthResponse(
                accessToken,
                "Bearer",
                jwtTokenService.getAccessTokenExpiresInSeconds()
        );
    }

    @Override
    @Transactional
    public AuthResponse refresh(HttpServletRequest request,
                                HttpServletResponse response) {

        String rawRefreshToken = refreshCookieService.extractRefreshToken(request)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_MISSING));

        RefreshTokenResult rotated =
                refreshTokenService.rotate(rawRefreshToken, request);

        refreshCookieService.addRefreshCookie(
                response,
                rotated.rawToken(),
                rotated.rememberMe(),
                Duration.between(OffsetDateTime.now(), rotated.expiresAt())
        );

        String accessToken =
                jwtTokenService.generateAccessToken(UserPrincipal.create(rotated.user()));

        return new AuthResponse(
                accessToken,
                "Bearer",
                jwtTokenService.getAccessTokenExpiresInSeconds()
        );
    }

    @Override
    @Transactional
    public void logout(UserPrincipal principal,
                       HttpServletRequest request,
                       HttpServletResponse response) {

        if (principal != null) {
            refreshTokenService.revokeAllByUserId(principal.getId());
        } else {
            refreshCookieService.extractRefreshToken(request)
                    .ifPresent(refreshTokenService::revokeByRawToken);
        }

        refreshCookieService.clearRefreshCookie(response);
    }

    private String buildRedirectUrl(String appBaseUrl) {
        if (appBaseUrl == null || appBaseUrl.isBlank()) {
            appBaseUrl = "http://localhost:5173";
        }
        return appBaseUrl + "/login?verified=true";
    }
}
