package com.brotherhood.scipubtts.auth.security.oauth2;

import com.brotherhood.scipubtts.user.entity.Role;
import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import com.brotherhood.scipubtts.auth.security.UserPrincipal;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Override
    @Transactional // Đảm bảo an toàn dữ liệu và tránh lỗi Lazy Loading Chắt chẽ
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processGoogleUser(oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    private OAuth2User processGoogleUser(OAuth2User oAuth2User) {
        String email = (String) oAuth2User.getAttributes().get("email");
        String givenName = (String) oAuth2User.getAttributes().get("given_name");
        String familyName = (String) oAuth2User.getAttributes().get("family_name");
        String flowMode = resolveFlowMode();

        if (!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info"),
                    "Email not found from Google");
        }

        Boolean googleEmailVerified = (Boolean) oAuth2User.getAttributes().get("email_verified");

        if (!Boolean.TRUE.equals(googleEmailVerified)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("google_email_not_verified"),
                    "Google account email is not verified");
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            if (HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_FLOW_MODE_LOGIN.equals(flowMode)) {
                throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_user_not_found"),
                        "Account not found. Please register first");
            }

            user = new User();
            user.setEmail(email);
            user.setUsername(email);
            user.setFirstName(givenName);
            user.setLastName(familyName);
            user.setRole(Role.RESEARCHER);
            user.setEmailVerified(true);
            user.setPasswordHash(null);
            user.setGoogleLinked(true);
            user.setBanned(false);
        } else {
            if (user.isBanned()) {
                throw new OAuth2AuthenticationException(new OAuth2Error("account_banned"),
                        "Account is banned");
            }
            if (!StringUtils.hasText(user.getFirstName())) {
                user.setFirstName(givenName);
            }
            if (!StringUtils.hasText(user.getLastName())) {
                user.setLastName(familyName);
            }
            user.setGoogleLinked(true);
        }

        userRepository.save(user);
        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private String resolveFlowMode() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_FLOW_MODE_LOGIN;
        }

        return authorizationRequestRepository
                .loadFlowMode(servletRequestAttributes.getRequest())
                .orElse(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_FLOW_MODE_LOGIN);
    }
}
