package com.brotherhood.scipubtts.auth.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;

    private String tokenType;

    private long expiresInSeconds;


    public String accessToken() {
        return accessToken;
    }

    public String tokenType() {
        return tokenType;
    }

    public long expiresInSeconds() {
        return expiresInSeconds;
    }
    @Override
    public String toString()
    {
        return "AuthResponse[" +
                "accessToken=***" +
                ", tokenType=" + tokenType +
                ", expiresInSeconds=" + expiresInSeconds +
                "]";
    }
}
