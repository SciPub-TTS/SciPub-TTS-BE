package com.brotherhood.scipubtts.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    // ===== AUTH =====
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED,
            "Incorrect email or password."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,
            "Authentication is required to access this resource."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN,
            "You do not have permission to access this resource."),
    EMAIL_EXISTS(HttpStatus.CONFLICT, "Email already exists"),

    ACCOUNT_BANNED(HttpStatus.FORBIDDEN,
            "This account has been banned."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN,
            "Please verify your email address before logging in."),
    LOCAL_PASSWORD_NOT_AVAILABLE(HttpStatus.BAD_REQUEST,
            "This account does not support local password authentication."),

    REFRESH_TOKEN_MISSING(HttpStatus.UNAUTHORIZED,
            "Refresh token is missing."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED,
            "Invalid refresh token."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,
            "Refresh token has expired."),
    REFRESH_TOKEN_REVOKED(HttpStatus.UNAUTHORIZED,
            "Refresh token has been revoked."),

    CURRENT_PASSWORD_INVALID(HttpStatus.BAD_REQUEST,
            "The current password you entered is incorrect."),
    PASSWORD_CONFIRMATION_NOT_MATCH(HttpStatus.BAD_REQUEST,
            "Password confirmation does not match."),
    PASSWORD_REUSE_NOT_ALLOWED(HttpStatus.BAD_REQUEST,
            "The new password cannot be the same as your current password."),
    PASSWORD_TOO_WEAK(HttpStatus.BAD_REQUEST,
            "The password does not meet the minimum security requirements."),

    PASSWORD_RESET_CODE_INVALID(HttpStatus.BAD_REQUEST,
            "Invalid verification code."),
    PASSWORD_RESET_CODE_EXPIRED(HttpStatus.BAD_REQUEST,
            "Verification code has expired."),
    PASSWORD_RESET_CODE_ATTEMPTS_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS,
            "You have exceeded the maximum number of attempts allowed."),
    PASSWORD_RESET_GRANT_INVALID(HttpStatus.BAD_REQUEST,
            "Invalid password reset grant token."),
    PASSWORD_RESET_GRANT_EXPIRED(HttpStatus.BAD_REQUEST,
            "Password reset grant token has expired."),

    REQUEST_BODY_REQUIRED(HttpStatus.BAD_REQUEST,
            "Request body is required."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS,
            "Too many requests. Please try again later."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "User not found."),
    INVALID_USER_INFO(HttpStatus.BAD_REQUEST, "Email not found from Google provider."),
    OAUTH2_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "No account is associated with this social email address."),

    // ===== EMAIL =====
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "The provided email address format is invalid."),
    EMAIL_VERIFICATION_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "Invalid email verification token."),
    EMAIL_VERIFICATION_TOKEN_ALREADY_USED(HttpStatus.BAD_REQUEST, "This email verification token has already been used."),
    EMAIL_VERIFICATION_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "Email verification token has expired."),

    // ===== OPENALEX =====
    OPENALEX_ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND,
            "OpenAlex entity not found"),
    OPENALEX_SERVICE_ERROR(HttpStatus.SERVICE_UNAVAILABLE,
            "OpenAlex service returned empty or invalid data."),
    OPENALEX_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "Could not parse response from OpenAlex"),
    OPENALEX_REQUIRED(HttpStatus.BAD_REQUEST,
            "Open Alex id is required."),
    OPENALEX_REQUEST_FAILED(HttpStatus.SERVICE_UNAVAILABLE,
            "OpenAlex request failed after multiple attempts"),
    RETRY_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR,
            "The retry process was interrupted."),
    OPENALEX_GATEWAY_ERROR(HttpStatus.BAD_GATEWAY,
            "Failed to retrieve publication data from OpenAlex"),

    // ===== BOOKMARK =====
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "Bookmark not found."),
    BOOKMARK_REQUIRED(HttpStatus.BAD_REQUEST, "Bookmark id is required."),
    BOOKMARK_ALREADY_EXISTS(HttpStatus.CONFLICT, "This paper has already been bookmarked."),
    BOOKMARK_ACCESS_DENIED(HttpStatus.FORBIDDEN, "You do not have permission to modify this bookmark.");

    private final HttpStatus status;
    private final String message;

}