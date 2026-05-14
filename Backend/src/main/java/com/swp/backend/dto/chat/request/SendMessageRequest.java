package com.swp.backend.dto.chat.request;

import java.time.LocalDateTime;

public record SendMessageRequest (
        String content,
        LocalDateTime sendAt
){
}
