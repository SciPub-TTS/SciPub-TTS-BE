package com.brotherhood.scipubtts.auth.service.impl;

import com.brotherhood.scipubtts.auth.dto.request.CompleteGoogleRegisterRequest;
import com.brotherhood.scipubtts.auth.dto.request.LoginRequest;
import com.brotherhood.scipubtts.auth.dto.request.RegisterLocalRequest;
import com.brotherhood.scipubtts.auth.dto.response.AuthResponse;
import com.brotherhood.scipubtts.auth.dto.response.GoogleSignupPreviewResponse;
import com.brotherhood.scipubtts.auth.dto.response.RefreshTokenResult;
import com.brotherhood.scipubtts.auth.entity.GoogleSignupToken;
import com.brotherhood.scipubtts.auth.repository.GoogleSignupTokenRepository;
import com.brotherhood.scipubtts.auth.service.*;
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
    private final AuthSessionService authSessionService;
    private final GoogleSignupTokenRepository googleSignupTokenRepository;
    private final SecureValueService secureValueService;

    private final RefreshTokenService refreshTokenService;
    private final RefreshCookieService refreshCookieService;

    @Value("${app.backend-base-url}")
    private String backendBaseUrl;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @Override
    @Transactional
    public String registerLocal(RegisterLocalRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
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
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_VERIFICATION_TOKEN_INVALID));

        if (verificationToken.isUsed()) {
            throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_TOKEN_ALREADY_USED);
        }

        if (verificationToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_TOKEN_EXPIRED);
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

        String passwordHash = user.getPasswordHash();

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (user.isBanned()) {
            throw new BusinessException(ErrorCode.ACCOUNT_BANNED);
        }

        if (!user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.email(),
                        request.password()
                )
        );

        return authSessionService.issueSession(
                user,
                request.rememberMe(),
                httpRequest,
                httpResponse
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

    @Override
    @Transactional
    public GoogleSignupPreviewResponse previewGoogleRegister(String rawToken) {
        GoogleSignupToken token = googleSignupTokenRepository
                .findByTokenHash(secureValueService.sha256(rawToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_GOOGLE_SIGNUP_TOKEN));

        if (token.isUsed()) {
            throw new BusinessException(ErrorCode.GOOGLE_SIGNUP_TOKEN_ALREADY_USED);
        }

        if (token.isExpired()) {
            throw new BusinessException(ErrorCode.GOOGLE_SIGNUP_TOKEN_EXPIRED);
        }

        return new GoogleSignupPreviewResponse(
                token.getEmail(),
                token.getFirstName(),
                token.getLastName()
        );
    }

    @Override
    @Transactional
    public AuthResponse completeGoogleRegister(
            CompleteGoogleRegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_NOT_MATCH);
        }

        GoogleSignupToken token = googleSignupTokenRepository
                .findByTokenHash(secureValueService.sha256(request.googleSignupToken()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_GOOGLE_SIGNUP_TOKEN));

        if (token.isUsed()) {
            throw new BusinessException(ErrorCode.GOOGLE_SIGNUP_TOKEN_ALREADY_USED);
        }

        if (token.isExpired()) {
            throw new BusinessException(ErrorCode.GOOGLE_SIGNUP_TOKEN_EXPIRED);
        }

        if (userRepository.existsByEmail(token.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }

        User user = User.builder()
                .email(token.getEmail())
                .username(token.getEmail())
                .firstName(token.getFirstName())
                .lastName(token.getLastName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.RESEARCHER)
                .emailVerified(true)
                .googleLinked(true)
                .banned(false)
                .build();

        userRepository.save(user);

        token.setUsedAt(OffsetDateTime.now());
        googleSignupTokenRepository.save(token);

        return authSessionService.issueSession(
                user,
                Boolean.TRUE.equals(request.rememberMe()),
                httpRequest,
                httpResponse
        );
    }



    private String buildRedirectUrl(String appBaseUrl) {
        String resolvedBaseUrl = appBaseUrl;

        if (resolvedBaseUrl == null || resolvedBaseUrl.isBlank()) {
            resolvedBaseUrl = frontendBaseUrl;
        }

        return trimTrailingSlash(resolvedBaseUrl) + "/login?verified=true";
    }

    private String trimTrailingSlash(String value) {
        return value.replaceAll("/+$", "");
    }
}
