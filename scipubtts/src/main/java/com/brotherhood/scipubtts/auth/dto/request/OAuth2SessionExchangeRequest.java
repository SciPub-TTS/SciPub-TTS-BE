package com.brotherhood.scipubtts.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OAuth2SessionExchangeRequest(
        @NotBlank(message = "rawRefreshToken is required")
        String rawRefreshToken
) {
}
