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
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.Optional;

@Component
@Slf4j
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String OAUTH2_FLOW_MODE_COOKIE_NAME = "oauth2_flow_mode";
    public static final String OAUTH2_FLOW_MODE_LOGIN = "login";
    public static final String OAUTH2_FLOW_MODE_REGISTER = "register";
    private static final String FLOW_MODE_REQUEST_PARAM = "flow_mode";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
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
            deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            return;
        }

        String flowMode = normalizeFlowMode(request.getParameter(FLOW_MODE_REQUEST_PARAM));
        if (flowMode != null) {
            authorizationRequest = OAuth2AuthorizationRequest
                    .from(authorizationRequest)
                    .attributes(attributes -> attributes.put(OAUTH2_FLOW_MODE_COOKIE_NAME, flowMode))
                    .build();
        }

        String serializedRequest = Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(authorizationRequest));

        Cookie cookie = new Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serializedRequest);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
        response.addCookie(cookie);
    }

    public void saveFlowMode(HttpServletResponse response, String mode) {
        String normalizedMode = normalizeFlowMode(mode);
        if (normalizedMode == null) {
            return;
        }

        Cookie cookie = new Cookie(OAUTH2_FLOW_MODE_COOKIE_NAME, normalizedMode);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
        response.addCookie(cookie);
    }

    public Optional<String> loadFlowMode(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        if (authorizationRequest != null) {
            String modeFromRequest = normalizeFlowMode(
                    (String) authorizationRequest.getAttributes().get(OAUTH2_FLOW_MODE_COOKIE_NAME)
            );
            if (modeFromRequest != null) {
                return Optional.of(modeFromRequest);
            }
        }

        return getCookie(request, OAUTH2_FLOW_MODE_COOKIE_NAME)
                .map(Cookie::getValue)
                .map(this::normalizeFlowMode)
                .filter(StringUtils::hasText);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        OAuth2AuthorizationRequest authRequest = this.loadAuthorizationRequest(request);
        if (getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME).isPresent()) {
            deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        }
        if (getCookie(request, OAUTH2_FLOW_MODE_COOKIE_NAME).isPresent()) {
            deleteCookie(request, response, OAUTH2_FLOW_MODE_COOKIE_NAME);
        }
        return authRequest;
    }

    private Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return Optional.of(cookie);
            }
        }

        return Optional.empty();
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }

    private <T> T deserialize(String cookieValue, Class<T> cls) {
        return cls.cast(SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookieValue)));
    }

    private OAuth2AuthorizationRequest safelyDeserialize(String cookieValue, HttpServletRequest request) {
        try {
            return deserialize(cookieValue, OAuth2AuthorizationRequest.class);
        } catch (IllegalArgumentException | SerializationException ex) {
            log.warn(
                    "Ignoring invalid OAuth2 authorization request cookie for {} {}",
                    request.getMethod(),
                    request.getRequestURI()
            );
            return null;
        }
    }

    private String normalizeFlowMode(String mode) {
        if (!StringUtils.hasText(mode)) {
            return null;
        }

        String normalizedMode = mode.trim().toLowerCase();
        if (OAUTH2_FLOW_MODE_LOGIN.equals(normalizedMode)
                || OAUTH2_FLOW_MODE_REGISTER.equals(normalizedMode)) {
            return normalizedMode;
        }

        return null;
    }
}
