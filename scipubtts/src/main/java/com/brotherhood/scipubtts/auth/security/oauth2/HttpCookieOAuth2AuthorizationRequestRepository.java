package com.brotherhood.scipubtts.auth.security.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Optional;

@Component
@Slf4j
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getCookie(request)
                .map(cookie -> safelyDeserialize(cookie.getValue(), request))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (authorizationRequest == null) {
            deleteCookie(request, response);
            return;
        }

        String serializedRequest = Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(authorizationRequest));

        Cookie cookie = new Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serializedRequest);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
        response.addCookie(cookie);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        OAuth2AuthorizationRequest authRequest = this.loadAuthorizationRequest(request);
        if (getCookie(request).isPresent()) {
            deleteCookie(request, response);
        }
        return authRequest;
    }

    private Optional<Cookie> getCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)) {
                return Optional.of(cookie);
            }
        }

        return Optional.empty();
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }

    private OAuth2AuthorizationRequest deserialize(String cookieValue) {
        return SerializationUtils.deserialize(
                Base64.getUrlDecoder().decode(cookieValue)
        );
    }
    private OAuth2AuthorizationRequest safelyDeserialize(String cookieValue, HttpServletRequest request) {
        try {
            return deserialize(cookieValue);
        } catch (IllegalArgumentException | SerializationException ex) {
            log.warn(
                    "Ignoring invalid OAuth2 authorization request cookie for {} {}",
                    request.getMethod(),
                    request.getRequestURI()
            );
            return null;
        }
    }
}
