package com.swp.backend.dto.coordinator.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record TakeListResponse (
        UUID requestID,
        String phone,
        String name,
        String status,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
){
}
