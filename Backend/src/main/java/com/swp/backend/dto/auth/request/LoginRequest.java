package com.swp.backend.dto.auth.request;

public record LoginRequest (
        String phone,
        String password
){
}
