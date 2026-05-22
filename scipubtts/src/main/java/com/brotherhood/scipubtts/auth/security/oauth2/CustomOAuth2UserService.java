package com.brotherhood.scipubtts.auth.security.oauth2;

import com.brotherhood.scipubtts.user.entity.Role;
import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import com.brotherhood.scipubtts.auth.security.UserPrincipal;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
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
        Boolean emailVerified = (Boolean) oAuth2User.getAttributes().get("email_verified");
        String fullName = (String) oAuth2User.getAttributes().get("name");
        String givenName = (String) oAuth2User.getAttributes().get("given_name");
        String familyName = (String) oAuth2User.getAttributes().get("family_name");

        if (!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info"),
                    "Email not found from Google");
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setUsername(email);
            user.setFirstName(givenName);
            user.setLastName(familyName);
            user.setRole(Role.RESEARCHER);
            user.setPasswordHash(null);
            user.setEmailVerified(Boolean.TRUE.equals(emailVerified));
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
            if (Boolean.TRUE.equals(emailVerified)) {
                user.setEmailVerified(true);
            }
            user.setGoogleLinked(true);
        }

        userRepository.save(user);
        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }
}
