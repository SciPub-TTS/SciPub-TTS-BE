package com.brotherhood.scipubtts.common.openalex.logging;

import com.brotherhood.scipubtts.auth.security.CustomUserDetailsService;
import com.brotherhood.scipubtts.auth.security.UserPrincipal;
import com.brotherhood.scipubtts.auth.security.jwt.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OpenAlexLoggingInterceptor implements ClientHttpRequestInterceptor {

    public static final int MAX_QUERY_PARAMS_LENGTH = 4_000;
    public static final int MAX_ERROR_LOG_LENGTH = 2_000;
    private static final String MASKED_VALUE = "[MASKED]";
    private static final Set<String> SECRET_QUERY_PARAM_NAMES = Set.of(
            "api_key",
            "apikey",
            "access_token",
            "token",
            "authorization",
            "auth",
            "bearer",
            "client_secret",
            "secret",
            "password"
    );

    private final OpenAlexApiCallLogService logService;
    private final JwtTokenService jwtTokenService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        OffsetDateTime startedAt = OffsetDateTime.now();
        long startedNanos = System.nanoTime();
        Integer responseStatus = null;
        String errorLog = null;

        try {
            ClientHttpResponse response = execution.execute(request, body);
            responseStatus = response.getStatusCode().value();
            return response;
        } catch (IOException | RuntimeException exception) {
            errorLog = sanitizeError(exception);
            throw exception;
        } finally {
            OffsetDateTime finishedAt = OffsetDateTime.now();
            saveLog(request, responseStatus, errorLog, startedAt, finishedAt, startedNanos);
        }
    }

    private void saveLog(
            HttpRequest request,
            Integer responseStatus,
            String errorLog,
            OffsetDateTime startedAt,
            OffsetDateTime finishedAt,
            long startedNanos
    ) {
        OpenAlexApiCallLog apiCallLog = new OpenAlexApiCallLog();
        OpenAlexCallContextSnapshot context = OpenAlexCallContext.current();
        Caller caller = resolveCaller(context);

        URI uri = request.getURI();
        apiCallLog.setCallerType(caller.callerType());
        apiCallLog.setUserId(caller.userId());
        apiCallLog.setJobId(context != null ? context.jobId() : null);
        apiCallLog.setJobType(context != null ? blankToNull(context.jobType()) : null);
        apiCallLog.setMethod(request.getMethod().name());
        apiCallLog.setEndpoint(resolveEndpoint(uri));
        apiCallLog.setQueryParams(sanitizeQuery(uri.getRawQuery()));
        apiCallLog.setResponseStatus(responseStatus);
        apiCallLog.setDurationMs(Duration.ofNanos(System.nanoTime() - startedNanos).toMillis());
        apiCallLog.setStartedAt(startedAt);
        apiCallLog.setFinishedAt(finishedAt);
        apiCallLog.setErrorLog(errorLog);

        logService.saveBestEffort(apiCallLog);
    }

    private Caller resolveCaller(OpenAlexCallContextSnapshot context) {
        if (context != null && context.forceSystem()) {
            return new Caller("SYSTEM", null);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            UUID userId = userPrincipal.getId();
            if (userId != null) {
                return new Caller("USER", userId);
            }
        }

        UUID bearerUserId = resolveUserIdFromCurrentRequestBearerToken();
        if (bearerUserId != null) {
            return new Caller("USER", bearerUserId);
        }

        return new Caller("GUEST", null);
    }

    private UUID resolveUserIdFromCurrentRequestBearerToken() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }

        HttpServletRequest request = servletRequestAttributes.getRequest();
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(bearer) || !bearer.startsWith("Bearer ")) {
            return null;
        }

        try {
            String token = bearer.substring(7);
            UUID userId = jwtTokenService.getUserId(token);
            customUserDetailsService.loadUserById(userId);
            return userId;
        } catch (Exception exception) {
            return null;
        }
    }

    private String resolveEndpoint(URI uri) {
        String rawPath = uri.getRawPath();
        if (StringUtils.hasText(rawPath)) {
            return rawPath;
        }

        return "/";
    }

    private String sanitizeQuery(String rawQuery) {
        if (!StringUtils.hasText(rawQuery)) {
            return null;
        }

        StringBuilder sanitized = new StringBuilder();
        String[] pairs = rawQuery.split("&");
        for (String pair : pairs) {
            if (sanitized.length() > 0) {
                sanitized.append('&');
            }

            int equalsIndex = pair.indexOf('=');
            String rawName = equalsIndex >= 0 ? pair.substring(0, equalsIndex) : pair;
            String rawValue = equalsIndex >= 0 ? pair.substring(equalsIndex + 1) : "";
            String decodedName = decode(rawName);

            sanitized.append(rawName);
            if (equalsIndex >= 0) {
                sanitized.append('=');
                sanitized.append(isSecretQueryParam(decodedName) ? MASKED_VALUE : rawValue);
            }
        }

        return truncate(sanitized.toString(), MAX_QUERY_PARAMS_LENGTH);
    }

    private boolean isSecretQueryParam(String name) {
        if (!StringUtils.hasText(name)) {
            return false;
        }

        String normalized = name.trim().toLowerCase(Locale.ROOT).replace("-", "_");
        return SECRET_QUERY_PARAM_NAMES.contains(normalized)
                || normalized.endsWith("_token")
                || normalized.endsWith("_secret")
                || normalized.endsWith("_key");
    }

    private String sanitizeError(Exception exception) {
        String message = exception.getMessage();
        String summary = exception.getClass().getSimpleName()
                + (StringUtils.hasText(message) ? ": " + message : "");
        return truncate(summary, MAX_ERROR_LOG_LENGTH);
    }

    private String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            return value;
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private record Caller(String callerType, UUID userId) {
    }
}
