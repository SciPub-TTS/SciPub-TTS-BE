package com.brotherhood.scipubtts.common.exception;

import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.config.AuthProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final AuthProperties authProperties;


    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        String message = ErrorCode.ACCESS_DENIED.getMessage();
        if (authProperties.isExposeSecurityErrorDetails() && accessDeniedException.getMessage() != null) {
            message = message + ":" + accessDeniedException.getMessage();
        }

        ResponseObject body = new ResponseObject(
                HttpStatus.FORBIDDEN.value(),
                message,
                Map.of("path", request.getRequestURI())
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
