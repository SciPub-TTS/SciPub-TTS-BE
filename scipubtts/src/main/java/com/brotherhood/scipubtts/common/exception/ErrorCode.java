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

    ACCOUNT_BANNED(HttpStatus.FORBIDDEN,
            "This account has been banned."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN,
            "Please verify your email address before logging in."),
    EMAIL_EXISTS(HttpStatus.FORBIDDEN,
            "The email has been exist."),
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
    INVALID_SEARCH_FILTER_COMBINATION(HttpStatus.BAD_REQUEST,
            "%s"),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS,
            "Too many requests. Please try again later."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "User not found."),
    INVALID_USER_INFO(HttpStatus.BAD_REQUEST, "Email not found from Google provider."),
    OAUTH2_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "No account is associated with this social email address."),

    INVALID_GOOGLE_SIGNUP_TOKEN(HttpStatus.BAD_REQUEST,
            "The registration token is invalid."),
    GOOGLE_SIGNUP_TOKEN_ALREADY_USED(HttpStatus.BAD_REQUEST,
            "This registration token has already been used."),
    GOOGLE_SIGNUP_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST,
            "This registration token has expired."),
    // ===== ADMIN =====
    ADMIN_SELF_ACTION_NOT_ALLOWED(HttpStatus.FORBIDDEN,
            "Admin cannot perform this action on their own account."),
    ADMIN_TARGET_NOT_ALLOWED(HttpStatus.FORBIDDEN,
            "Admin accounts cannot be banned or unbanned."),
    ADMIN_ACCOUNT_ALREADY_BANNED(HttpStatus.CONFLICT,
            "This account has already been banned."),
    ADMIN_ACCOUNT_NOT_BANNED(HttpStatus.CONFLICT,
            "This account is not banned."),

    // ===== EMAIL =====
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "The provided email address format is invalid."),
    EMAIL_VERIFICATION_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "Invalid email verification token."),
    EMAIL_VERIFICATION_TOKEN_ALREADY_USED(HttpStatus.BAD_REQUEST, "This email verification token has already been used."),
    EMAIL_VERIFICATION_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "Email verification token has expired."),

    // ===== OPENALEX =====
    OPENALEX_ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND,
            "OpenAlex entity not found."),
    OPENALEX_SERVICE_ERROR(HttpStatus.SERVICE_UNAVAILABLE,
            "OpenAlex service returned empty or invalid data."),
    OPENALEX_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "Could not parse response from OpenAlex."),
    OPENALEX_REQUIRED(HttpStatus.BAD_REQUEST,
            "OpenAlex id is required."),
    OPENALEX_REQUEST_FAILED(HttpStatus.SERVICE_UNAVAILABLE,
            "OpenAlex request failed after multiple attempts."),
    RETRY_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR,
            "The retry process was interrupted."),
    OPENALEX_GATEWAY_ERROR(HttpStatus.BAD_GATEWAY,
            "Failed to retrieve publication data from OpenAlex."),
    JSON_SERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An error occurred while processing system data."),

    // ===== STATISTIC =====
    TOPIC_REQUEST_INVALID(HttpStatus.BAD_REQUEST,
            "Request start time and end time must not be null."),
    TOPIC_NOT_FOUND(HttpStatus.BAD_REQUEST,
            "Can not find topic with this id."),

    // ===== BOOKMARK =====
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "Bookmark not found."),
    BOOKMARK_REQUIRED(HttpStatus.BAD_REQUEST, "Bookmark id is required."),
    BOOKMARK_ALREADY_EXISTS(HttpStatus.CONFLICT, "This paper has already been bookmarked."),
    BOOKMARK_ACCESS_DENIED(HttpStatus.FORBIDDEN, "You do not have permission to modify this bookmark."),

    // ===== FEED =====
    SOCIAL_POST_NOT_FOUND(HttpStatus.NOT_FOUND,
            "Insight post not found."),
    SOCIAL_POST_EXCEEDS_REFERENCE_LIMIT(HttpStatus.BAD_REQUEST,
            "An insight post can reference a maximum of 3 papers.");

    private final HttpStatus status;
    private final String message;

}
