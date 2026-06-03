package com.brotherhood.scipubtts.auth.security.oauth2;

import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Mặc định lỗi chung nếu không phân loại được
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        // Bốc tách lỗi được ném ra từ tầng OAuth2 cấu hình bên dưới
        if (exception instanceof OAuth2AuthenticationException oAuth2Exception) {
            String subErrorCode = oAuth2Exception.getError().getErrorCode();

            if ("invalid_user_info".equals(subErrorCode)) {
                errorCode = ErrorCode.INVALID_USER_INFO;
            } else if ("oauth2_user_not_found".equals(subErrorCode)) {
                errorCode = ErrorCode.OAUTH2_USER_NOT_FOUND;
            }
        }

        response.setStatus(errorCode.getStatus().value());

        // Format y hệt cấu hình trong GlobalExceptionHandler của bạn
        ResponseObject body = new ResponseObject(
                errorCode.getStatus().value(),
                errorCode.getMessage(),
                null
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
