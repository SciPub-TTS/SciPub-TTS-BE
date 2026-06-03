package com.brotherhood.scipubtts.common.exception;

import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.config.AuthProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final AuthProperties authProperties;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String message = ErrorCode.UNAUTHORIZED.getMessage();
        Object detail = request.getAttribute("auth_error");

        if (authProperties.isExposeSecurityErrorDetails()) {
            if (detail instanceof String s && !s.isBlank()) {
                message = s;
            } else if (authException != null && authException.getMessage() != null) {
                message = authException.getMessage();
            }
        }

        ResponseObject body = new ResponseObject(
                HttpStatus.UNAUTHORIZED.value(),
                message,
                Map.of("path",  request.getRequestURI())
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
