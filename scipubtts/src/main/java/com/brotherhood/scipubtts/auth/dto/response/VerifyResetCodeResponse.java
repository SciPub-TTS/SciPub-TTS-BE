package com.brotherhood.scipubtts.auth.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyResetCodeResponse {
    private String resetGrantToken;

    private long expiresInSeconds;


    public String resetGrantToken() {
        return resetGrantToken;
    }

    public long expiresInSeconds() {
        return expiresInSeconds;
    }
    @Override
    public String toString()
    {
        return "VerifyResetCodeResponse[" +
                "resetGrantToken=***" +
                ", expiresInSeconds=" + expiresInSeconds +
                "]";
    }
}
