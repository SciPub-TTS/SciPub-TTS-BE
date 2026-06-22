package com.brotherhood.scipubtts.auth.dto.response;

import lombok.NonNull;

public record VerifyResetCodeResponse(
        String resetGrantToken,
        long expiresInSeconds
) {
    @Override
    @NonNull
    public String toString()
    {
        return "VerifyResetCodeResponse[" +
                "resetGrantToken=***" +
                ", expiresInSeconds=" + expiresInSeconds +
                "]";
    }
}
