package com.brotherhood.scipubtts.auth.dto.response;

public record VerifyResetCodeResponse(
        String resetGrantToken,
        long expiresInSeconds
) {

    @Override
    public String toString()
    {
        return "VerifyResetCodeResponse[" +
                "resetGrantToken=***" +
                ", expiresInSeconds=" + expiresInSeconds +
                "]";
    }
}