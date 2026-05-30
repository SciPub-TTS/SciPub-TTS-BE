package com.brotherhood.scipubtts.auth.service.impl;

import com.brotherhood.scipubtts.auth.dto.response.RefreshTokenResult;
import com.brotherhood.scipubtts.auth.entity.RefreshToken;
import com.brotherhood.scipubtts.auth.repository.RefreshTokenRepository;
import com.brotherhood.scipubtts.auth.service.RefreshTokenService;
import com.brotherhood.scipubtts.auth.service.SecureValueService;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.config.AuthProperties;
import com.brotherhood.scipubtts.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureValueService secureValueService;
    private final AuthProperties authProperties;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository,
                                  SecureValueService secureValueService,
                                  AuthProperties authProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.secureValueService = secureValueService;
        this.authProperties = authProperties;
    }

    @Override
    @Transactional
    public RefreshTokenResult issue(User user, boolean rememberMe, HttpServletRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        Duration ttl = rememberMe
                ? authProperties.rememberMeRefreshTokenTtl()
                : authProperties.refreshTokenTtl();

        String rawToken = secureValueService.generateOpaqueToken();

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(secureValueService.sha256(rawToken))
                .rememberMe(rememberMe)
                .issuedAt(now)
                .expiredAt(now.plus(ttl))
                .build();

        refreshTokenRepository.save(token);

        return new RefreshTokenResult(user, rawToken, rememberMe, token.getExpiredAt());
    }

    @Override
    @Transactional
    public RefreshTokenResult rotate(String rawRefreshToken, HttpServletRequest request) {
        OffsetDateTime now = OffsetDateTime.now();

        RefreshToken current = refreshTokenRepository.findByTokenHashForUpdate(
                        secureValueService.sha256(rawRefreshToken)
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

        //Nếu trường này khác null, nghĩa là token này đã từng được sử dụng trước đó rồi, hoặc user đã bấm Logout. Việc một token đã hủy đột ngột xuất hiện lại là dấu hiệu của việc Hacker đang cố dùng lại token cũ ăn trộm được. Hệ thống sẽ lập tức chặn lại.
        if (current.getRevokedAt() != null) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_REVOKED);
        }

        //Nếu thời gian hết hạn nằm trước thời gian hiện tại (now), hệ thống từ chối vì token quá hạn.
        if (current.getExpiredAt().isBefore(now)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        //Sau khi xác nhận token cũ hoàn toàn hợp lệ, hệ thống tiến hành "khai tử" nó bằng cách cập nhật mốc thời gian hủy setRevokedAt(now). Từ giây phút này, token này chính thức phế bỏ, không bao giờ dùng lại được nữa.
        current.setRevokedAt(now);
        refreshTokenRepository.save(current);

        Duration ttl = current.isRememberMe()
                ? authProperties.rememberMeRefreshTokenTtl()
                : authProperties.refreshTokenTtl();

        //Hệ thống sinh ra một chuỗi token ngẫu nhiên bảo mật mới (nextRawToken).
        //Đóng gói vào đối tượng RefreshToken mới, thực hiện băm bảo mật (sha256) và lưu xuống Database để phục vụ cho lần xoay vòng tiếp theo.
        String nextRawToken = secureValueService.generateOpaqueToken();

        RefreshToken next = RefreshToken.builder()
                .user(current.getUser())
                .tokenHash(secureValueService.sha256(nextRawToken))
                .rememberMe(current.isRememberMe())
                .issuedAt(now)
                .expiredAt(now.plus(ttl))
                .build();

        refreshTokenRepository.save(next);

        return new RefreshTokenResult(
                next.getUser(),
                nextRawToken,
                next.isRememberMe(),
                next.getExpiredAt()
        );
    }

    @Override
    @Transactional
    public void revokeAllByUserId(UUID userId) {
        refreshTokenRepository.revokeAllActiveByUserId(
                userId,
                OffsetDateTime.now()
        );
    }

    @Override
    @Transactional
    public void revokeByRawToken(String rawRefreshToken) {
        if (!StringUtils.hasText(rawRefreshToken)) {
            return;
        }

        refreshTokenRepository.findByTokenHashForUpdate(
                secureValueService.sha256(rawRefreshToken)
        ).ifPresent(token -> revokeAllByUserId(token.getUser().getId()));
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        if (userAgent == null) {
            return null;
        }
        return userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent;
    }
}
