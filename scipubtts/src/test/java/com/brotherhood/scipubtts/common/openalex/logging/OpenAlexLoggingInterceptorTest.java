package com.brotherhood.scipubtts.common.openalex.logging;

import com.brotherhood.scipubtts.auth.security.CustomUserDetailsService;
import com.brotherhood.scipubtts.auth.security.UserPrincipal;
import com.brotherhood.scipubtts.auth.security.jwt.JwtTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAlexLoggingInterceptorTest {

    @Mock
    private OpenAlexApiCallLogService logService;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
        OpenAlexCallContext.runWithContext(null, () -> {
        });
    }

    @Test
    void logsSuccessfulSystemCallAndMasksCredentialQueryParams() throws Exception {
        OpenAlexLoggingInterceptor interceptor = interceptor();
        MockClientHttpRequest request = new MockClientHttpRequest(
                HttpMethod.GET,
                URI.create("https://api.openalex.org/works?filter=topics.id:T1&API_KEY=secret&access_token=abc")
        );

        interceptor.intercept(
                request,
                new byte[0],
                (httpRequest, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK)
        );

        OpenAlexApiCallLog log = captureLog();
        assertThat(log.getCallerType()).isEqualTo("SYSTEM");
        assertThat(log.getUserId()).isNull();
        assertThat(log.getMethod()).isEqualTo("GET");
        assertThat(log.getEndpoint()).isEqualTo("/works");
        assertThat(log.getQueryParams()).contains("filter=topics.id:T1");
        assertThat(log.getQueryParams()).contains("API_KEY=[MASKED]");
        assertThat(log.getQueryParams()).contains("access_token=[MASKED]");
        assertThat(log.getResponseStatus()).isEqualTo(200);
        assertThat(log.getDurationMs()).isNotNegative();
        assertThat(log.getErrorLog()).isNull();
    }

    @Test
    void logsAuthenticatedUserWhenNoForcedSystemContextExists() throws Exception {
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(
                userId,
                "researcher@example.test",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_RESEARCHER"))
        );
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(principal, null, principal.getAuthorities())
        );
        OpenAlexLoggingInterceptor interceptor = interceptor();
        MockClientHttpRequest request = new MockClientHttpRequest(
                HttpMethod.GET,
                URI.create("https://api.openalex.org/authors/A123")
        );

        interceptor.intercept(
                request,
                new byte[0],
                (httpRequest, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK)
        );

        OpenAlexApiCallLog log = captureLog();
        assertThat(log.getCallerType()).isEqualTo("USER");
        assertThat(log.getUserId()).isEqualTo(userId);
        assertThat(log.getEndpoint()).isEqualTo("/authors/A123");
    }

    @Test
    void forcedSystemContextOverridesAuthenticatedUserAndStoresJobMetadata() {
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(
                userId,
                "admin@example.test",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(principal, null, principal.getAuthorities())
        );
        OpenAlexLoggingInterceptor interceptor = interceptor();
        MockClientHttpRequest request = new MockClientHttpRequest(
                HttpMethod.GET,
                URI.create("https://api.openalex.org/works")
        );

        OpenAlexCallContext.runAsSystemJob(jobId, "FEED_SYNC", () -> {
            try {
                interceptor.intercept(
                        request,
                        new byte[0],
                        (httpRequest, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK)
                );
            } catch (IOException exception) {
                throw new AssertionError(exception);
            }
        });

        OpenAlexApiCallLog log = captureLog();
        assertThat(log.getCallerType()).isEqualTo("SYSTEM");
        assertThat(log.getUserId()).isNull();
        assertThat(log.getJobId()).isEqualTo(jobId);
        assertThat(log.getJobType()).isEqualTo("FEED_SYNC");
    }

    @Test
    void logsTransportFailureWithoutMaskingOriginalException() {
        OpenAlexLoggingInterceptor interceptor = interceptor();
        MockClientHttpRequest request = new MockClientHttpRequest(
                HttpMethod.GET,
                URI.create("https://api.openalex.org/works")
        );
        IOException original = new IOException("connection refused");

        assertThatThrownBy(() -> interceptor.intercept(
                request,
                new byte[0],
                (httpRequest, body) -> {
                    throw original;
                }
        )).isSameAs(original);

        OpenAlexApiCallLog log = captureLog();
        assertThat(log.getResponseStatus()).isNull();
        assertThat(log.getErrorLog()).contains("IOException");
        assertThat(log.getErrorLog()).contains("connection refused");
    }

    @Test
    void resolvesUserFromCurrentRequestBearerTokenWhenSecurityContextIsEmpty() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = "token-value";
        UserPrincipal principal = new UserPrincipal(
                userId,
                "admin@example.test",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader("Authorization", "Bearer " + token);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));
        when(jwtTokenService.getUserId(token)).thenReturn(userId);
        when(customUserDetailsService.loadUserById(userId)).thenReturn(principal);
        OpenAlexLoggingInterceptor interceptor = interceptor();
        MockClientHttpRequest request = new MockClientHttpRequest(
                HttpMethod.GET,
                URI.create("https://api.openalex.org/works")
        );

        interceptor.intercept(
                request,
                new byte[0],
                (httpRequest, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK)
        );

        OpenAlexApiCallLog log = captureLog();
        assertThat(log.getCallerType()).isEqualTo("USER");
        assertThat(log.getUserId()).isEqualTo(userId);
    }

    private OpenAlexApiCallLog captureLog() {
        ArgumentCaptor<OpenAlexApiCallLog> captor = ArgumentCaptor.forClass(OpenAlexApiCallLog.class);
        verify(logService).saveBestEffort(captor.capture());
        return captor.getValue();
    }

    private OpenAlexLoggingInterceptor interceptor() {
        return new OpenAlexLoggingInterceptor(logService, jwtTokenService, customUserDetailsService);
    }
}
