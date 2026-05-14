package com.swp.backend.dto.chat.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse(
    UUID id,
    UUID senderId,
    String senderName,
    String senderRole,
    String content,
    LocalDateTime sentAt
) {}