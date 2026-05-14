package com.swp.backend.dto.chat.response;

import java.util.List;

public record ChatHistoryResponse (
    List<MessageResponse> messages
){
}
