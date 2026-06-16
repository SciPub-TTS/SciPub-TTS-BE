package com.brotherhood.scipubtts.auth.security.jwt;

import com.brotherhood.scipubtts.auth.security.CustomUserDetailsService;
import com.brotherhood.scipubtts.auth.security.UserPrincipal;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<String> PUBLIC_PATH_PATTERNS = List.of(
            "/api/auth/register",
            "/api/auth/register/google/**",
            "/api/auth/login",
            "/api/auth/oauth2/exchange",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/api/auth/oauth2/google",
            "/api/auth/verify-email",
            "/api/auth/forgot-password/**",
            "/api/search/**",
            "/api/papers/**",
            "/api/authors/**",
            "/api/topics/**",
            "/api/statistic/**",
            "/oauth2/**",
            "/login/oauth2/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/error"
    );

    private final JwtTokenService jwtTokenService;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                   CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenService = jwtTokenService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String path = request.getServletPath();
        for (String pattern : PUBLIC_PATH_PATTERNS) {
            if (PATH_MATCHER.match(pattern, path)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Team note:
        // This log is intentionally kept for debugging token flow. If production logs are shared outside
        // the backend team, remove or mask this header because it contains the raw bearer token.
        log.info("Authorization header = {}", request.getHeader("Authorization"));

        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            String token = bearer.substring(7);

            try {
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UUID userId = jwtTokenService.getUserId(token);
                    UserPrincipal principal =
                            (UserPrincipal) customUserDetailsService.loadUserById(userId);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    principal.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (ExpiredJwtException ex) {
                request.setAttribute("auth_error", "Access token expired");
            } catch (BusinessException ex) {
                request.setAttribute("auth_error", ex.getMessage());
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
                request.setAttribute("auth_error", "Invalid Access token ");
            }
        }

        filterChain.doFilter(request, response);
    }
}
