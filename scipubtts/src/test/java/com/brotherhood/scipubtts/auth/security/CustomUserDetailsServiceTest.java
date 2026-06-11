package com.brotherhood.scipubtts.auth.security;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.user.entity.Role;
import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new CustomUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsernameRejectsBannedUser() {
        User user = bannedUser();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(user.getEmail()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCOUNT_BANNED);
    }

    @Test
    void loadUserByIdRejectsBannedUserAccessToken() {
        User user = bannedUser();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userDetailsService.loadUserById(user.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCOUNT_BANNED);
    }

    private User bannedUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("banned@example.com")
                .role(Role.RESEARCHER)
                .passwordHash("hash")
                .banned(true)
                .build();
    }
}
