package com.brotherhood.scipubtts.auth.service.impl;

import com.brotherhood.scipubtts.auth.dto.response.VerifyResetCodeResponse;
import com.brotherhood.scipubtts.auth.entity.PasswordResetChallenge;
import com.brotherhood.scipubtts.auth.entity.PasswordResetGrant;
import com.brotherhood.scipubtts.auth.repository.PasswordResetChallengeRepository;
import com.brotherhood.scipubtts.auth.repository.PasswordResetGrantRepository;
import com.brotherhood.scipubtts.auth.service.RefreshTokenService;
import com.brotherhood.scipubtts.auth.service.SecureValueService;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.config.AuthProperties;
import com.brotherhood.scipubtts.auth.service.PasswordResetCodeRequestedEvent;
import com.brotherhood.scipubtts.auth.service.PasswordRecoveryService;
import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryServiceImpl implements PasswordRecoveryService {

    private final UserRepository userRepository;
    private final PasswordResetChallengeRepository challengeRepository;
    private final PasswordResetGrantRepository grantRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureValueService secureValueService;
    private final AuthProperties authProperties;
    private final ApplicationEventPublisher eventPublisher;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public void requestReset(String email, HttpServletRequest request) {
        // Hook rate limit ở đây nếu cần:
        // throttleService.checkForgotPasswordRequest(email, clientIp(request));

        Optional<User> optionalUser = userRepository.findByEmail(email);

        // luôn trả generic ra ngoài, không lộ email có tồn tại hay không
        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();

        // nếu account chỉ dùng Google, không có local password thì bỏ qua âm thầm
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();

        challengeRepository.invalidateActiveByUserId(user.getId(), now);
        grantRepository.revokeActiveByUserId(user.getId(), now);

        String rawCode = secureValueService.generateOtpCode(
                authProperties.getPasswordResetCodeLength()
        );

        PasswordResetChallenge challenge = PasswordResetChallenge.builder()
                .user(user)
                .emailSnapshot(user.getEmail())
                .codeHash(passwordEncoder.encode(rawCode))
                .attemptCount(0)
                .maxAttempts(authProperties.getPasswordResetMaxAttempts())
                .expiresAt(now.plus(authProperties.passwordResetCodeTtl()))
                .build();

        challengeRepository.save(challenge);

        eventPublisher.publishEvent(
                new PasswordResetCodeRequestedEvent(user.getEmail(), rawCode)
        );
    }

    @Override
    @Transactional
    public VerifyResetCodeResponse verifyCode(String email,
                                              String code,
                                              HttpServletRequest request) {
        // Hook rate limit ở đây nếu cần:
        // throttleService.checkForgotPasswordVerify(email, clientIp(request));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.PASSWORD_RESET_CODE_INVALID));

        PasswordResetChallenge challenge = challengeRepository
                .findTopByUserIdAndInvalidatedAtIsNullOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PASSWORD_RESET_CODE_INVALID));

        OffsetDateTime now = OffsetDateTime.now();

        if (challenge.getAttemptCount() >= challenge.getMaxAttempts()) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_CODE_ATTEMPTS_EXCEEDED);
        }

        if (challenge.isExpired()) {
            challenge.setInvalidatedAt(now);
            challengeRepository.save(challenge);
            throw new BusinessException(ErrorCode.PASSWORD_RESET_CODE_EXPIRED);
        }

        String normalizedCode = code.trim().toUpperCase(Locale.ROOT);

        if (!passwordEncoder.matches(normalizedCode, challenge.getCodeHash())) {
            challenge.setAttemptCount(challenge.getAttemptCount() + 1);
            if (challenge.getAttemptCount() >= challenge.getMaxAttempts()) {
                challenge.setInvalidatedAt(now);
            }
            challengeRepository.save(challenge);
            throw new BusinessException(
                    challenge.getAttemptCount() >= challenge.getMaxAttempts()
                            ? ErrorCode.PASSWORD_RESET_CODE_ATTEMPTS_EXCEEDED
                            : ErrorCode.PASSWORD_RESET_CODE_INVALID
            );
        }

        challenge.setVerifiedAt(now);
        challenge.setInvalidatedAt(now);
        challengeRepository.save(challenge);

        //Sau khi xác thực thành công OTP, nó sẽ hủy OTP đó đi và sinh ra một rawGrant (Opaque Token - chuỗi mã thông báo quyền). Trả chuỗi rawGrant này về cho Frontend. Frontend sẽ dùng rawGrant này để gọi API resetPassword ở bước tiếp theo.
        grantRepository.revokeActiveByUserId(user.getId(), now);

        String rawGrant = secureValueService.generateOpaqueToken();

        PasswordResetGrant grant = PasswordResetGrant.builder()
                .user(user)
                .challengeId(challenge)
                .tokenHash(secureValueService.sha256(rawGrant))
                .expiredAt(now.plus(authProperties.passwordResetGrantTtl()))
                .build();

        grantRepository.save(grant);

        return new VerifyResetCodeResponse(
                rawGrant,
                authProperties.passwordResetGrantTtl().toSeconds()
        );
    }

    @Override
    @Transactional
    public void resetPassword(String resetGrantToken,
                              String newPassword,
                              String confirmNewPassword) {

        if (!Objects.equals(newPassword, confirmNewPassword)) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_NOT_MATCH);
        }

        String grantHash = secureValueService.sha256(resetGrantToken);

        PasswordResetGrant grant = grantRepository.findByTokenHash(grantHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.PASSWORD_RESET_GRANT_INVALID));

        OffsetDateTime now = OffsetDateTime.now();

        if (grant.getRevokedAt() != null || grant.getConsumedAt() != null) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_GRANT_INVALID);
        }

        if (grant.isExpired()) {
            grant.setRevokedAt(now);
            grantRepository.save(grant);
            throw new BusinessException(ErrorCode.PASSWORD_RESET_GRANT_EXPIRED);
        }

        User user = grant.getUser();

        if (user.getPasswordHash() != null &&
                passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_REUSE_NOT_ALLOWED);
        }

        //Mã hóa mật khẩu mới của user (bằng thuật toán như BCrypt hoặc Argon2 thông qua passwordEncoder) thành một chuỗi ký tự không thể dịch ngược, sau đó lưu đè vào cột password_hash của user đó trong bảng dữ liệu.
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        //Để ngăn chặn lỗi Replay Attack (Tấn công phát lại). Nếu hacker bằng cách nào đó ăn trộm được chuỗi resetGrantToken này từ mạng của user, chúng cũng không thể gửi lại request một lần nữa để đổi mật khẩu của user sang một pass khác, vì hệ thống check thấy chiếc vé này đã "bị dùng rồi".
        grant.setConsumedAt(now);
        grantRepository.save(grant);

        //Quét sạch một lần nữa trong Database xem user này còn cái mã OTP nào chưa hết hạn (challenge) hay còn cái vé thông hành nào khác (grant) đang nằm chờ không, lập tức hủy kích hoạt toàn bộ (invalidate/revoke). Việc này đảm bảo rằng ngay cả khi có một mã OTP hoặc grant nào đó bị rò rỉ ra ngoài, chúng cũng không thể được sử dụng để tấn công nữa vì đã bị hủy sạch sẽ rồi.
        challengeRepository.invalidateActiveByUserId(user.getId(), now);
        grantRepository.revokeActiveByUserId(user.getId(), now);

        //Khi một người phải đi đổi mật khẩu, khả năng cao là tài khoản của họ đã bị lộ (hoặc họ vừa đăng nhập ở một máy tính công cộng nào đó mà quên thoát). Lệnh này sẽ lập tức đá văng (đăng xuất bắt buộc) tài khoản này ra khỏi tất cả các thiết bị đang đăng nhập hiện tại (như điện thoại, laptop khác, trình duyệt khác, hay máy của hacker).
        refreshTokenService.revokeAllByUserId(user.getId());

//        eventPublisher.publishEvent(
//                new PasswordResetCompletedEvent(user.getEmail())
//        );
    }

}
