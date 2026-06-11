package com.brotherhood.scipubtts.auth.service.impl;

import com.brotherhood.scipubtts.auth.entity.RefreshToken;
import com.brotherhood.scipubtts.auth.repository.RefreshTokenRepository;
import com.brotherhood.scipubtts.auth.service.SecureValueService;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.config.AuthProperties;
import com.brotherhood.scipubtts.user.entity.Role;
import com.brotherhood.scipubtts.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private SecureValueService secureValueService;

    @Mock
    private HttpServletRequest request;

    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenServiceImpl(
                refreshTokenRepository,
                secureValueService,
                new AuthProperties()
        );
    }

    @Test
    void rotateRejectsBannedUserAndRevokesActiveTokens() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("banned@example.com")
                .role(Role.RESEARCHER)
                .banned(true)
                .build();
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash("hash")
                .issuedAt(OffsetDateTime.now().minusMinutes(1))
                .expiredAt(OffsetDateTime.now().plusHours(1))
                .build();

        when(secureValueService.sha256("raw-refresh")).thenReturn("hash");
        when(refreshTokenRepository.findByTokenHashForUpdate("hash")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.rotate("raw-refresh", request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCOUNT_BANNED);

        verify(refreshTokenRepository).revokeAllActiveByUserId(eq(user.getId()), any(OffsetDateTime.class));
        verify(refreshTokenRepository, never()).save(token);
    }
}
